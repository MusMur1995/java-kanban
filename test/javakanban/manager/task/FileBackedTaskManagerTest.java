package javakanban.manager.task;

import javakanban.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;
    private File file;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = File.createTempFile("test", ".csv", tempDir.toFile());
        manager = FileBackedTaskManager.createForTest(file);
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        Epic epic = manager.createEpic(new Epic("Epic 1", "Epic description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Sub description", epic.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> loadedTasks = loadedManager.getAllTasks();
        assertEquals(2, loadedTasks.size(), "Должно быть 2 задачи");

        List<Epic> loadedEpics = loadedManager.getAllEpics();
        assertEquals(1, loadedEpics.size(), "Должен быть 1 эпик");

        List<Subtask> loadedSubtasks = loadedManager.getAllSubtasks();
        assertEquals(1, loadedSubtasks.size(), "Должна быть 1 подзадача");

        Subtask loadedSubtask = loadedSubtasks.get(0);
        assertEquals(epic.getId(), loadedSubtask.getEpicId(), "Должна сохраниться связь с эпиком");

        List<Subtask> epicSubtasks = loadedManager.getSubtasksByEpic(epic.getId());
        assertEquals(1, epicSubtasks.size(), "Эпик должен иметь подзадачи");
    }

    @Test
    void shouldSaveAndLoadTaskData() {
        Task originalTask = new Task("Test Task", "Test Description");
        originalTask.setStatus(TaskStatus.IN_PROGRESS);
        Task createdTask = manager.createTask(originalTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getTaskById(createdTask.getId());

        assertNotNull(loadedTask, "Задача должна быть загружена");
        assertEquals(createdTask.getId(), loadedTask.getId(), "ID должен совпадать");
        assertEquals("Test Task", loadedTask.getName(), "Название должно совпадать");
        assertEquals("Test Description", loadedTask.getDescription(), "Описание должно совпадать");
        assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus(), "Статус должен совпадать");
    }

    @Test
    void shouldMaintainTaskOrderAfterLoad() {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Epic description"));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(2, loadedManager.getAllTasks().size(), "Должно быть 2 задачи");
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть 1 эпик");

        assertNotNull(loadedManager.getTaskById(task1.getId()), "Должна находиться задача 1");
        assertNotNull(loadedManager.getTaskById(task2.getId()), "Должна находиться задача 2");
        assertNotNull(loadedManager.getEpicById(epic.getId()), "Должен находиться эпик");
    }

    @Test
    @DisplayName("Тест вызова статического метода с несуществующим файлом")
    void shouldCreateEmptyManagerWhenFileNotExists() {
        File nonExistentFile = new File(tempDir.toFile(), "non_existent.csv");

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(nonExistentFile);

        assertTrue(manager.getAllTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(manager.getAllEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(manager.getAllSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    @DisplayName("Проверяем, что счётчик ID восстанавливается правильно после загрузки из файла")
    void shouldRestoreIdCounterCorrectly() {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1")); // ID = 1
        Task task2 = manager.createTask(new Task("Task 2", "Description 2")); // ID = 2
        Epic epic = manager.createEpic(new Epic("Epic 1", "Epic description")); // ID = 3

        int lastUsedId = epic.getId();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task newTask = loadedManager.createTask(new Task("New Task", "New Description"));

        assertEquals(lastUsedId + 1, newTask.getId(),
                "Счётчик ID должен восстановиться и продолжить с правильного числа");
        assertEquals(4, newTask.getId(), "Новая задача должна получить ID = 4");
    }
}