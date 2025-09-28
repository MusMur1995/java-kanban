package javakanban.manager.task;

import javakanban.manager.Managers;
import javakanban.models.*;
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

    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        // Создаём временный файл
        File file = File.createTempFile("test", ".csv", tempDir.toFile());

        // Создаём менеджер и сразу сохраняем пустое состояние
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        manager.save(); // явно сохраняем пустое состояние

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что всё пусто
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        File file = File.createTempFile("test", ".csv", tempDir.toFile());
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);

        // Создаём задачи
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        Epic epic = manager.createEpic(new Epic("Epic 1", "Epic description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Sub description", epic.getId()));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем восстановление
        List<Task> loadedTasks = loadedManager.getAllTasks();
        assertEquals(2, loadedTasks.size(), "Должно быть 2 задачи");

        List<Epic> loadedEpics = loadedManager.getAllEpics();
        assertEquals(1, loadedEpics.size(), "Должен быть 1 эпик");

        List<Subtask> loadedSubtasks = loadedManager.getAllSubtasks();
        assertEquals(1, loadedSubtasks.size(), "Должна быть 1 подзадача");

        // Проверяем связи
        Subtask loadedSubtask = loadedSubtasks.get(0);
        assertEquals(epic.getId(), loadedSubtask.getEpicId(), "Должна сохраниться связь с эпиком");

        List<Subtask> epicSubtasks = loadedManager.getSubtasksByEpic(epic.getId());
        assertEquals(1, epicSubtasks.size(), "Эпик должен иметь подзадачи");
    }

    @Test
    void shouldSaveAndLoadTaskData() throws IOException {
        File file = File.createTempFile("test", ".csv", tempDir.toFile());
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);

        // Создаём задачу с конкретными данными
        Task originalTask = new Task("Test Task", "Test Description");
        originalTask.setStatus(TaskStatus.IN_PROGRESS);
        Task createdTask = manager.createTask(originalTask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getTaskById(createdTask.getId());

        // Проверяем, что все данные сохранились
        assertNotNull(loadedTask, "Задача должна быть загружена");
        assertEquals(createdTask.getId(), loadedTask.getId(), "ID должен совпадать");
        assertEquals("Test Task", loadedTask.getName(), "Название должно совпадать");
        assertEquals("Test Description", loadedTask.getDescription(), "Описание должно совпадать");
        assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus(), "Статус должен совпадать");
    }

    @Test
    void shouldMaintainTaskOrderAfterLoad() throws IOException {
        File file = File.createTempFile("test", ".csv", tempDir.toFile());
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);

        // Создаём несколько задач
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Epic description"));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что порядок и количество сохранилось
        assertEquals(2, loadedManager.getAllTasks().size(), "Должно быть 2 задачи");
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть 1 эпик");

        // Проверяем, что можем получить задачи по ID
        assertNotNull(loadedManager.getTaskById(task1.getId()), "Должна находиться задача 1");
        assertNotNull(loadedManager.getTaskById(task2.getId()), "Должна находиться задача 2");
        assertNotNull(loadedManager.getEpicById(epic.getId()), "Должен находиться эпик");
    }
}