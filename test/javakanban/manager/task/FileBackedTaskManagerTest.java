package javakanban.manager.task;

import javakanban.exceptions.ManagerSaveException;
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

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            file = File.createTempFile("test", ".csv", tempDir.toFile());
            return FileBackedTaskManager.loadFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void initManager() {
        manager = createTaskManager();
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasks() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        Epic epic = manager.createEpic(new Epic("Epic 1", "Epic description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Sub description", epic.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> loadedTasks = loadedManager.getAllTasks();
        assertEquals(2, loadedTasks.size());

        List<Epic> loadedEpics = loadedManager.getAllEpics();
        assertEquals(1, loadedEpics.size());

        List<Subtask> loadedSubtasks = loadedManager.getAllSubtasks();
        assertEquals(1, loadedSubtasks.size());

        Subtask loadedSubtask = loadedSubtasks.get(0);
        assertEquals(epic.getId(), loadedSubtask.getEpicId());

        List<Subtask> epicSubtasks = loadedManager.getSubtasksByEpic(epic.getId());
        assertEquals(1, epicSubtasks.size());
    }

    @Test
    void shouldSaveAndLoadTaskData() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);
        Task originalTask = new Task("Test Task", "Test Description");
        originalTask.setStatus(TaskStatus.IN_PROGRESS);
        Task createdTask = manager.createTask(originalTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getTaskById(createdTask.getId());

        assertNotNull(loadedTask);
        assertEquals(createdTask.getId(), loadedTask.getId());
        assertEquals("Test Task", loadedTask.getName());
        assertEquals("Test Description", loadedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    @DisplayName("Тест вызова статического метода с несуществующим файлом")
    void shouldCreateEmptyManagerWhenFileNotExists() {
        File nonExistentFile = new File(tempDir.toFile(), "non_existent.csv");

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(nonExistentFile);

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    @DisplayName("Проверяем, что счётчик ID восстанавливается правильно после загрузки из файла")
    void shouldRestoreIdCounterCorrectly() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Epic description"));

        int lastUsedId = epic.getId();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task newTask = loadedManager.createTask(new Task("New Task", "New Description"));

        assertEquals(lastUsedId + 1, newTask.getId());
        assertEquals(4, newTask.getId());
    }

    @Test
    @DisplayName("Должен выбрасывать ManagerSaveException при ошибке доступа к файлу")
    void shouldThrowManagerSaveException_onFileAccessError() {
        File tempFile = new File(tempDir.toFile(), "test.csv");

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);
        manager.createTask(new Task("Test", "Description"));

        assertTrue(tempFile.exists());

        tempFile.setReadOnly();

        assertThrows(ManagerSaveException.class, () -> {
            manager.createTask(new Task("Test2", "Description2"));
        });

        tempFile.setWritable(true);
    }
}