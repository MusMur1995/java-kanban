package javakanban.http;

import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerHistoryTest extends HttpBaseTest{

    @Test
    @DisplayName("Получение пустой истории через HTTP GET")
    void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertNotNull(history);
        assertEquals(0, history.length);
    }

    @Test
    @DisplayName("Получение истории после просмотра задач")
    void testGetHistoryWithTasks() throws IOException, InterruptedException {

        Task task1 = manager.createTask(new Task("Задача 1", "Описание 1"));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2"));
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(4, history.length);

        assertEquals("Задача 1", history[0].getName());
        assertEquals("Задача 2", history[1].getName());
        assertEquals("Эпик", history[2].getName());
        assertEquals("Подзадача", history[3].getName());
    }

    @Test
    @DisplayName("История содержит только уникальные задачи (без дубликатов)")
    void testHistoryWithoutDuplicates() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Задача", "Описание"));

        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);

        assertEquals(1, history.length);
        assertEquals("Задача", history[0].getName());
    }

    @Test
    @DisplayName("История обновляется при удалении задач")
    void testHistoryAfterTaskDeletion() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task("Задача 1", "Описание"));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание"));

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        manager.deleteTaskById(task1.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);

        assertEquals(1, history.length);
        assertEquals("Задача 2", history[0].getName());
    }

    @Test
    @DisplayName("История очищается при удалении всех задач")
    void testEmptyHistoryAfterClear() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task("Задача 1", "Описание"));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание"));

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        manager.deleteAllTasks();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, history.length);
    }

    @Test
    @DisplayName("Порядок истории соответствует порядку просмотра")
    void testHistoryOrder() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task("Первая", "Описание"));
        Task task2 = manager.createTask(new Task("Вторая", "Описание"));
        Task task3 = manager.createTask(new Task("Третья", "Описание"));

        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task3.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(3, history.length);

        assertEquals("Вторая", history[0].getName()); // первая просмотренная
        assertEquals("Первая", history[1].getName()); // вторая просмотренная
        assertEquals("Третья", history[2].getName()); // последняя просмотренная
    }

    @Test
    @DisplayName("Некорректный метод для истории (405)")
    void testInvalidMethodForHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}