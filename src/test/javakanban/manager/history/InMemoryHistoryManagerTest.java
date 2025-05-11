package javakanban.manager.history;

import javakanban.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    @DisplayName("add должен добавлять задачи в историю")
    void shouldAddTasksToHistory() {
        Task task = new Task("Task", "Description");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    //убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    @DisplayName("История хранит неизменённую задачу после изменения оригинала")
    void historyShouldStoreUnchangedTaskAfterModification() {
        Task task = new Task("Task", "Description");

        historyManager.add(task);

        task.setName("Modified Task");

        List<Task> history = historyManager.getHistory();
        assertEquals("Task", history.get(0).getName(),
                "В истории должна сохраняться неизменная копия задачи");
    }

    @Test
    @DisplayName("null не должен добавляться в историю")
    void shouldNotAddNullTask() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    @DisplayName("история должна хранить не более 10 последних задач")
    void historyShouldContainOnlyLast10Tasks() {
        for (int i = 1; i <= 15; i++) {
            historyManager.add(new Task("Task" + i, "Desc" + i));
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size());
        assertEquals("Task6", history.get(0).getName());
        assertEquals("Task15", history.get(9).getName());
    }
}