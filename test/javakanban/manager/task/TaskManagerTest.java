package javakanban.manager.task;

import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.Task;
import javakanban.models.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createTaskManager();

    @Test
    @DisplayName("createTask() должен добавлять задачи, getTaskById() находить их по id")
    void createTask_returnTask_whenValidId() {
        Task task = new Task("Task", "Description");
        Task createdTask = manager.createTask(task);
        assertEquals(createdTask, manager.getTaskById(createdTask.getId()));
    }

    @Test
    @DisplayName("createEpic() должен добавлять эпики, getEpicById() находить их по id")
    void createEpic_returnEpic_whenValidId() {
        Epic epic = new Epic("Epic", "Epic Description");
        Epic createdEpic = manager.createEpic(epic);
        assertEquals(createdEpic, manager.getEpicById(createdEpic.getId()));
    }

    @Test
    @DisplayName("createSubtask() должен добавлять подзадачи, getSubtaskById() находить их по id")
    void createSubtask_returnSubtask_whenValidId() {
        Epic epic = manager.createEpic(new Epic("Epic", "Epic Description"));
        Subtask subtask = new Subtask("Subtask", "Subtask Description", epic.getId());
        Subtask createdSubtask = manager.createSubtask(subtask);
        assertEquals(createdSubtask, manager.getSubtaskById(createdSubtask.getId()));
    }

    @Test
    @DisplayName("deleteAllTasks() должен очищать список задач")
    void deleteAllTasks_returnTrue() {
        manager.createTask(new Task("Task1", "Desc1"));
        manager.createTask(new Task("Task2", "Desc2"));
        assertFalse(manager.getAllTasks().isEmpty());

        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    @DisplayName("updateSubtask() автоматически обновляет статус эпика при добавлении в него подзадач")
    void updateEpicStatus_changesStatus_whenSubtasksChange() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc", epic.getId());

        sub1.setStatus(TaskStatus.NEW);
        sub2.setStatus(TaskStatus.NEW);
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getStatus());

        sub1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(sub1);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());

        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);
        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    @DisplayName("deleteEpicById() должен удалять все подзадачи эпика")
    void deleteEpic_removesSubtasks_whenEpicDeleted() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        manager.deleteEpicById(epic.getId());

        assertNull(manager.getSubtaskById(sub1.getId()));
        assertNull(manager.getSubtaskById(sub2.getId()));
        assertNull(manager.getEpicById(epic.getId()));
    }

    @Test
    @DisplayName("Метод getSubtasksByEpic() должен возвращать все подзадачи указанного эпика")
    void getSubtasksByEpic_returnAllSubtasks_whenValidEpicId() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        List<Subtask> subtasks = manager.getSubtasksByEpic(epic.getId());
        assertEquals(2, subtasks.size());
    }

    @Test
    @DisplayName("Подзадача должна иметь существующий эпик")
    void subtask_shouldHaveExistingEpic() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());

        Subtask createdSubtask = manager.createSubtask(subtask);

        assertNotNull(createdSubtask);
        assertEquals(epic.getId(), createdSubtask.getEpicId());
        assertNotNull(manager.getEpicById(createdSubtask.getEpicId()));
    }

    @Test
    @DisplayName("Создание подзадачи с несуществующим эпиком должно возвращать null")
    void createSubtask_withNonExistentEpic_shouldReturnNull() {
        Subtask subtask = new Subtask("Subtask", "Description", 999);

        Subtask result = manager.createSubtask(subtask);

        assertNull(result);
    }

    @Test
    @DisplayName("Статус эпика NEW когда все подзадачи NEW")
    void epicStatus_shouldBeNew_whenAllSubtasksNew() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    @DisplayName("Статус эпика DONE когда все подзадачи DONE")
    void epicStatus_shouldBeDone_whenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    @DisplayName("Статус эпика IN_PROGRESS когда подзадачи NEW и DONE")
    void epicStatus_shouldBeInProgress_whenSubtasksNewAndDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        sub1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    @DisplayName("Статус эпика NEW когда нет подзадач")
    void epicStatus_shouldBeNew_whenNoSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    @DisplayName("Задачи не пересекаются, когда временные интервалы разделены")
    void tasks_shouldNotOverlap_whenTimeIntervalsSeparate() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofMinutes(60),
                LocalDateTime.of(2024, 1, 1, 10, 0));
        Task task2 = new Task("Task 2", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2024, 1, 1, 11, 30));

        manager.createTask(task1);
        assertDoesNotThrow(() -> manager.createTask(task2));
    }

    @Test
    @DisplayName("Задачи пересекаются, когда временные интервалы перекрываются")
    void tasks_shouldOverlap_whenTimeIntervalsOverlap() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofMinutes(120),
                LocalDateTime.of(2024, 1, 1, 10, 0));
        Task task2 = new Task("Task 2", "Description",
                Duration.ofMinutes(60),
                LocalDateTime.of(2024, 1, 1, 11, 0));

        manager.createTask(task1);
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2));
    }

    @Test
    @DisplayName("Задачи без времени не вызывают пересечений")
    void tasks_withoutTime_shouldNotCauseOverlap() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description",
                Duration.ofMinutes(60),
                LocalDateTime.of(2024, 1, 1, 10, 0));

        manager.createTask(task1);
        assertDoesNotThrow(() -> manager.createTask(task2));
    }

    @Test
    @DisplayName("Обновление задачи с пересекающимся временем должно вызывать исключение")
    void updateTask_withOverlappingTime_shouldThrowException() {
        Task task1 = manager.createTask(new Task("Task 1", "Description",
                Duration.ofMinutes(60),
                LocalDateTime.of(2024, 1, 1, 10, 0)));
        Task task2 = manager.createTask(new Task("Task 2", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2024, 1, 1, 14, 0)));

        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(task2));
    }
}