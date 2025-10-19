package javakanban.http;

import javakanban.exceptions.NotFoundException;
import javakanban.models.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest extends HttpBaseTest{

    public HttpTaskManagerTasksTest() {
    }

    @Test
    @DisplayName("Создание новой задачи через HTTP POST")
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Задача 1", "Описание к задаче 1");
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Задача 1", tasksFromManager.get(0).getName());
    }

    @Test
    @DisplayName("Получение всех задач через HTTP GET")
    void testGetAllTasks() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Test", "Description"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Test", tasks[0].getName());
    }

    @Test
    @DisplayName("Получение задачи по ID через HTTP GET")
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Test", "Description"));
        int taskId = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals(taskId, responseTask.getId());
        assertEquals("Test", responseTask.getName());
    }

    @Test
    @DisplayName("Попытка получения несуществующей задачи (404)")
    void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("Обновление задачи через HTTP POST")
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Original", "Desc"));
        Task updatedTask = new Task("Updated", "New desc");
        updatedTask.setId(task.getId());
        String taskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task taskFromManager = manager.getTaskById(task.getId());
        assertEquals("Updated", taskFromManager.getName());
    }

    @Test
    @DisplayName("Удаление задачи через HTTP DELETE")
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("To delete", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> manager.getTaskById(task.getId()));
    }

    @Test
    @DisplayName("Проверка конфликта времени задач (406)")
    void testTimeConflict() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task("Task 1", "Desc",
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 15, 10, 0)));

        Task task2 = new Task("Task 2", "Desc",
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 15, 10, 30));
        String taskJson = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    @DisplayName("Некорректный HTTP метод (405)")
    void testInvalidMethod() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}