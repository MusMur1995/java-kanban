import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.Task;
import javakanban.models.TaskStatus;
import javakanban.service.InMemoryTaskManager;
import javakanban.service.Managers;
import javakanban.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
    @Test
    @DisplayName("InMemoryTaskManager должен добавлять задачи и находить их по id")
    void shouldAddAndRetrieveTasksById() {

        Task task = new Task("Task", "Description");
        Task createdTask = manager.createTask(task);

        Epic epic = new Epic("Epic", "Epic Description");
        Epic createdEpic = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask Description", createdEpic.getId());
        Subtask createdSubtask = manager.createSubtask(subtask);

        assertInstanceOf(InMemoryTaskManager.class, manager);
        assertEquals(createdTask, manager.getTaskById(createdTask.getId()));
        assertEquals(createdEpic, manager.getEpicById(createdEpic.getId()));
        assertEquals(createdSubtask, manager.getSubtaskById(createdSubtask.getId()));
    }

    @Test
    @DisplayName("Удаление всех задач должно очищать список задач")
    void shouldDeleteAllTasks() {
        manager.createTask(new Task("Task1", "Desc1"));
        manager.createTask(new Task("Task2", "Desc2"));
        assertFalse(manager.getAllTasks().isEmpty());

        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    @DisplayName("Epic должен автоматически обновлять статус при добавлении подзадач")
    void epicStatusShouldBeUpdatedAccordingToSubtasks() {
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
    @DisplayName("Удаление эпика должно удалять все его подзадачи")
    void deletingEpicShouldDeleteItsSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        manager.deleteEpicById(epic.getId());

        assertNull(manager.getSubtaskById(sub1.getId()));
        assertNull(manager.getSubtaskById(sub2.getId()));
        assertNull(manager.getEpicById(epic.getId()));
    }

    @Test
    @DisplayName("Метод getSubtasksByEpic должен возвращать все подзадачи указанного эпика")
    void shouldReturnAllSubtasksOfEpic() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        List<Subtask> subtasks = manager.getSubtasksByEpic(epic.getId());
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(sub1));
        assertTrue(subtasks.contains(sub2));
    }

    @Test
    @DisplayName("История должна сохранять задачи после получения по ID")
    void historyShouldContainAccessedTasks() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }
}