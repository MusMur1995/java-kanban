package javakanban.manager.task;

import javakanban.manager.history.InMemoryHistoryManager;
import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.Task;
import javakanban.models.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
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
    @DisplayName("createSubtask() должен добавлять эпики, getSubtaskById() находить их по id")
    void createSubtask_returnSubtask_whenValidId() {

        Epic epic = new Epic("Epic", "Epic Description");
        Epic createdEpic = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask Description", createdEpic.getId());
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
    @DisplayName("getTaskById() должен сохранять задачи, в истории запросов, после получения по ID")
    void getHistory_containsTask_whenAccessedById() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();
        assertEquals(task, history.get(0));
    }
}