package javakanban.manager.task;

import javakanban.manager.history.InMemoryHistoryManager;
import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @BeforeEach
    void initManager() {
        manager = createTaskManager();
    }

    // оставил специфичные тесты для InMemoryTaskManagerTest

    @Test
    @DisplayName("Изменение имени задачи через сеттер не влияет на менеджер")
    void setName_doesNotAffectManager() {
        Task task = manager.createTask(new Task("Task", "Desc"));

        Task taskFromManager = manager.getTaskById(task.getId());
        taskFromManager.setName("New Name"); // Изменяем копию

        Task freshFromManager = manager.getTaskById(task.getId());

        assertNotEquals("New Name", freshFromManager.getName(),
                "Менеджер должен хранить исходное имя задачи");
    }

    @Test
    @DisplayName("getTaskById() должен сохранять копии задач в истории")
    void getHistory_containsTaskCopy_whenAccessedById() {
        Task task = manager.createTask(new Task("Task", "Desc"));

        Task fromManager = manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();
        assertFalse(history.isEmpty(), "История не должна быть пустой");

        Task historyTask = history.get(0);
        assertEquals(task.getId(), historyTask.getId());
        assertEquals(task.getName(), historyTask.getName());

        assertNotSame(task, historyTask);
        assertNotSame(fromManager, historyTask);
    }

    @Test
    @DisplayName("Удаление подзадачи удаляет её ID из Epic")
    void deleteSubtask_removesIdFromEpic() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", epic.getId()));

        manager.deleteSubtaskById(subtask.getId());
        assertFalse(epic.getSubtaskIds().contains(subtask.getId()),
                "ID подзадачи должен быть удалён из Epic");
    }
}