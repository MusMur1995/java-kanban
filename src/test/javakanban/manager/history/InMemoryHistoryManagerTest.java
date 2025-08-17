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

    //убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    @DisplayName("История хранит неизменённую задачу после изменения оригинала")
    void add_modifyOriginal_copyInHistoryUnchanged() {
        Task task = new Task("Task", "Description");

        historyManager.add(task);

        task.setName("Modified Task");

        List<Task> history = historyManager.getHistory();
        assertEquals("Task", history.get(0).getName(),
                "В истории должна сохраняться неизменная копия задачи");
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
        assertEquals(1, history.size(), "История должна содержать одну задачу");

        Epic epicFromHistory = (Epic) history.get(0);
        assertEquals("EpicTask", epicFromHistory.getName(), "Имя Epic должно совпадать");
        assertEquals("EpicDescription", epicFromHistory.getDescription(), "Описание Epic должно совпадать");
        assertEquals(1, epicFromHistory.getSubtaskIds().size(), "Epic должен содержать один подзадачный ID");
        assertEquals(101, epicFromHistory.getSubtaskIds().get(0), "ID подзадачи должен совпадать");
    }

    @Test
    @DisplayName("Добавление Subtask с полями")
    void add_subtaskWithFields_subtaskAdded() {
        Subtask subtask = new Subtask("Subtask", "SubDescription", 42);
        historyManager.add(subtask);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");

        Subtask subtaskFromHistory = (Subtask) history.get(0);
        assertEquals("Subtask", subtaskFromHistory.getName(), "Имя Subtask должно совпадать");
        assertEquals("SubDescription", subtaskFromHistory.getDescription(), "Описание Subtask должно совпадать");
        assertEquals(42, subtaskFromHistory.getEpicId(), "ID Epic для Subtask должен совпадать");
    }
}