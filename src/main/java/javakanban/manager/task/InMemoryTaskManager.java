package javakanban.manager.task;

import javakanban.manager.history.HistoryManager;
import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.Task;
import javakanban.models.TaskStatus;

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
        // Проверка пересечения по времени
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Задача '" + task.getName() + "' пересекается по времени с существующими задачами");
        }

        task.setId(++idCounter);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            // Проверка пересечения по времени
            if (hasTimeOverlap(task)) {
                throw new IllegalArgumentException("Обновленная задача '" + task.getName() + "' пересекается по времени с другими задачами");
            }

            Task existing = tasks.get(task.getId());

            existing.setName(task.getName());
            existing.setDescription(task.getDescription());
            existing.setStatus(task.getStatus());

            return existing;
        }
        return null;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
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
            return;
        }
        Epic existingEpic = epics.get(epic.getId());
        existingEpic.setName(epic.getName());
        existingEpic.setDescription(epic.getDescription());
        updateEpicStatus(existingEpic.getId());
    }

    @Override
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            return;
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

        calculateEpicTimes(epic, subtasks);
    }

    private void calculateEpicTimes(Epic epic, List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            epic.setCalculatedTimes(null, null, Duration.ZERO);
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
        epic.setCalculatedTimes(earliestStart, latestEnd, totalDuration);
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
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
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
            return null;
        }

        if (hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача '" + subtask.getName() + "' пересекается по времени с существующими задачами");
        }

        subtask.setId(++idCounter);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic.getId());
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            if (hasTimeOverlap(subtask)) {
                throw new IllegalArgumentException("Обновленная подзадача '" + subtask.getName() + "' пересекается по времени с другими задачами");
            }
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        if (!epics.containsKey(epicId)) {
            return new ArrayList<>();
        }
        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epics.get(epicId).getSubtaskIds()) {
            result.add(subtasks.get(subtaskId));
        }
        return result;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        Set<Task> prioritizedSet = new TreeSet<>((task1, task2) -> {
            if (task1.getStartTime() == null && task2.getStartTime() == null) {
                return Integer.compare(task1.getId(), task2.getId());
            }
            if (task1.getStartTime() == null) return 1;
            if (task2.getStartTime() == null) return -1;

            int timeComparison = task1.getStartTime().compareTo(task2.getStartTime());
            return timeComparison != 0 ? timeComparison : Integer.compare(task1.getId(), task2.getId());
        });

        prioritizedSet.addAll(tasks.values());
        prioritizedSet.addAll(subtasks.values());

        for (Epic epic : epics.values()) {
            if (epic.getStartTime() != null) {
                prioritizedSet.add(epic);
            }
        }

        return new ArrayList<>(prioritizedSet);
    }

    /**
     * Проверяет пересечение двух задач по времени выполнения
     */
    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }

        if (task1.getId() == task2.getId()) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    /**
     * Проверяет пересекается ли задача по времени с любыми существующими задачами
     * Использует отсортированный список для эффективности O(n)
     */
    private boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }

        List<Task> prioritized = getPrioritizedTasks();
        LocalDateTime newEnd = newTask.getEndTime();

        for (Task existingTask : prioritized) {
            if (existingTask.getStartTime() == null) continue;
            if (existingTask.getId() == newTask.getId()) continue;

            LocalDateTime existingStart = existingTask.getStartTime();

            if (existingStart.isAfter(newEnd)) {
                break;
            }

            if (isTasksOverlap(newTask, existingTask)) {
                return true;
            }
        }

        return false;
    }

}