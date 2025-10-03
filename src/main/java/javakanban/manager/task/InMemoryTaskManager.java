package javakanban.manager.task;

import javakanban.manager.history.HistoryManager;
import javakanban.models.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager;
    protected int idCounter = 0;

    private final Set<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
        if (task1.getStartTime() == null && task2.getStartTime() == null) {
            return Integer.compare(task1.getId(), task2.getId());
        }
        if (task1.getStartTime() == null) return 1;
        if (task2.getStartTime() == null) return -1;

        int timeComparison = task1.getStartTime().compareTo(task2.getStartTime());
        return timeComparison != 0 ? timeComparison : Integer.compare(task1.getId(), task2.getId());
    });

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //методы для Task
    @Override
    public List<Task> getAllTasks() {
        return tasks.values().stream()
                .map(Task::copy)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllTasks() {

        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
        prioritizedTasks.removeIf(task -> task.getType() == TaskType.TASK);
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {

            historyManager.add(task);

            return task.copy();
        }
        return null;
    }

    @Override
    public Task createTask(Task task) {
        validateNoTimeOverlap(task);
        task.setId(++idCounter);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача с ID " + task.getId() + " не найдена");
        }
        validateNoTimeOverlap(task);
        Task existing = tasks.get(task.getId());
        prioritizedTasks.remove(existing);
        existing.setName(task.getName());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        existing.setDuration(task.getDuration());
        existing.setStartTime(task.getStartTime());
        if (existing.getStartTime() != null) {
            prioritizedTasks.add(existing);
        }
        return existing;
    }

    @Override
    public void deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            throw new IllegalArgumentException("Задача с ID " + id + " не найдена");
        }
        Task task = tasks.remove(id);
        prioritizedTasks.remove(task);
        historyManager.remove(id);
    }

    //методы для Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (int subtaskId : epic.getSubtaskIds()) {
                historyManager.remove(subtaskId);
            }
        }
        subtasks.clear();
        epics.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(++idCounter);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Эпик с ID " + epic.getId() + " не найден");
        }
        Epic existingEpic = epics.get(epic.getId());
        existingEpic.setName(epic.getName());
        existingEpic.setDescription(epic.getDescription());
        updateEpicStatus(existingEpic.getId());
        calculateEpicTimes(existingEpic, getSubtasksByEpic(existingEpic.getId()));
    }

    @Override
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            throw new IllegalArgumentException("Эпик с ID " + id + " не найден");
        }
        Epic epic = epics.remove(id);
        List<Integer> subtaskIds = epic.getSubtaskIds();
        for (int subtaskId : subtaskIds) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        historyManager.remove(id);
    }

    //обновление статуса эпика
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasks = getSubtasksByEpic(epicId);

        if (subtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }
        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void calculateEpicTimes(Epic epic, List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() == null) continue;

            if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                earliestStart = subtask.getStartTime();
            }

            LocalDateTime subtaskEnd = subtask.getEndTime();
            if (subtaskEnd != null) {
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }
            }

            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }
        epic.setStartTime(earliestStart);
        epic.setEndTime(latestEnd);
        epic.setDuration(totalDuration);
    }

    //методы для subTask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {

        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.clear();
        prioritizedTasks.removeIf(task -> task.getType() == TaskType.SUBTASK);
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            calculateEpicTimes(epic, getSubtasksByEpic(epic.getId()));
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не найден");
        }

        validateNoTimeOverlap(subtask);

        subtask.setId(++idCounter);
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic.getId());
        calculateEpicTimes(epic, getSubtasksByEpic(epic.getId()));
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с ID " + subtask.getId() + " не найдена");
        }
        validateNoTimeOverlap(subtask);

        Subtask existing = subtasks.get(subtask.getId());
        prioritizedTasks.remove(existing);

        existing.setName(subtask.getName());
        existing.setDescription(subtask.getDescription());
        existing.setStatus(subtask.getStatus());
        existing.setDuration(subtask.getDuration());
        existing.setStartTime(subtask.getStartTime());

        if (existing.getStartTime() != null) {
            prioritizedTasks.add(existing);
        }
        updateEpicStatus(subtask.getEpicId());

        calculateEpicTimes(epics.get(subtask.getEpicId()),
                getSubtasksByEpic(subtask.getEpicId()));
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Подзадача с ID " + id + " не найдена");
        }
        Subtask subtask = subtasks.remove(id);
        prioritizedTasks.remove(subtask);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic.getId());
                calculateEpicTimes(epic, getSubtasksByEpic(epic.getId()));
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        if (!epics.containsKey(epicId)) {
            throw new IllegalArgumentException("Эпик с ID " + epicId + " не найден");
        }
        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epics.get(epicId).getSubtaskIds()) {
            result.add(subtasks.get(subtaskId));
        }
        return result;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void validateTimeOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return;
        }

        if (task1.getId() == task2.getId()) {
            return;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        if (!(end1.isBefore(start2) || end2.isBefore(start1))) {
            throw new IllegalArgumentException(
                    String.format("Задача '%s' пересекается по времени с существующей задачей '%s'",
                            task1.getName(), task2.getName())
            );
        }
    }

    private void validateTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null) {
            return;
        }

        List<Task> prioritized = getPrioritizedTasks();
        LocalDateTime newEnd = newTask.getEndTime();

        for (Task existingTask : prioritized) {

            validateTimeOverlap(newTask, existingTask);

            if (existingTask.getStartTime() != null && existingTask.getStartTime().isAfter(newEnd)) {
                break;
            }
        }
    }

    private void validateNoTimeOverlap(Task task) {
        validateTimeOverlap(task);
    }
}