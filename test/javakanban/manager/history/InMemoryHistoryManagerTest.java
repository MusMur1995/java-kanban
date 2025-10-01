package javakanban.manager.history;

import javakanban.models.Epic;
import javakanban.models.Subtask;
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
    void add_task_taskAddedToHistory() {
        Task task = new Task("Task", "Description");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    @DisplayName("История хранит неизменённую задачу после изменения оригинала")
    void add_modifyOriginal_copyInHistoryUnchanged() {
        Task task = new Task("Task", "Description");

        historyManager.add(task);

        task.setName("Modified Task");

        List<Task> history = historyManager.getHistory();
        assertEquals("Task", history.get(0).getName());
    }

    @Test
    @DisplayName("null не должен добавляться в историю")
    void add_null_notAddedToHistory() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    @DisplayName("Добавление Epic с полями")
    void add_epicWithFields_epicAdded() {
        Epic epic = new Epic("EpicTask", "EpicDescription");
        epic.addSubtaskId(101);
        historyManager.add(epic);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());

        Epic epicFromHistory = (Epic) history.get(0);
        assertEquals("EpicTask", epicFromHistory.getName());
        assertEquals("EpicDescription", epicFromHistory.getDescription());
        assertEquals(1, epicFromHistory.getSubtaskIds().size());
        assertEquals(101, epicFromHistory.getSubtaskIds().get(0));
    }

    @Test
    @DisplayName("Добавление Subtask с полями")
    void add_subtaskWithFields_subtaskAdded() {
        Subtask subtask = new Subtask("Subtask", "SubDescription", 42);
        historyManager.add(subtask);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());

        Subtask subtaskFromHistory = (Subtask) history.get(0);
        assertEquals("Subtask", subtaskFromHistory.getName());
        assertEquals("SubDescription", subtaskFromHistory.getDescription());
        assertEquals(42, subtaskFromHistory.getEpicId());
    }

    @Test
    @DisplayName("История должна быть пустой при создании")
    void history_shouldBeEmpty_onCreation() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Дублирование задачи в истории должно перемещать её в конец")
    void add_duplicateTask_shouldMoveToEnd() {
        Task task = new Task("Task", "Description");

        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    @DisplayName("Удаление задачи из начала истории")
    void remove_fromBeginning() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        Task task3 = new Task("Task 3", "Description");

        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }

    @Test
    @DisplayName("Удаление задачи из середины истории")
    void remove_fromMiddle() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        Task task3 = new Task("Task 3", "Description");

        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }

    @Test
    @DisplayName("Удаление задачи из конца истории")
    void remove_fromEnd() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        Task task3 = new Task("Task 3", "Description");

        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }
}