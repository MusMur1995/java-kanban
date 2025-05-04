import javakanban.models.Task;
import javakanban.service.HistoryManager;
import javakanban.service.InMemoryHistoryManager;
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

    @Test
    @DisplayName("getHistory должен возвращать копию списка")
    void getHistoryShouldReturnCopy() {
        Task task = new Task("Task", "Description");
        historyManager.add(task);

        List<Task> originalHistory = historyManager.getHistory();
        originalHistory.clear();

        List<Task> actualHistory = historyManager.getHistory();
        assertEquals(1, actualHistory.size());
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
