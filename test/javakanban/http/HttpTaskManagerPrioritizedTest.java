package javakanban.http;

import javakanban.models.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerPrioritizedTest extends HttpBaseTest {

    @Test
    @DisplayName("Получение пустого приоритизированного списка")
    void testGetEmptyPrioritized() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertNotNull(prioritized);
        assertEquals(0, prioritized.length);
    }

    @Test
    @DisplayName("Приоритизированный список сортируется по времени начала")
    void testPrioritizedOrderByStartTime() throws IOException, InterruptedException {

        Task task3 = manager.createTask(new Task("Утренняя", "Описание",
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 15, 9, 0)));
        Task task1 = manager.createTask(new Task("Вечерняя", "Описание",
                Duration.ofMinutes(90), LocalDateTime.of(2024, 1, 15, 18, 0)));
        Task task2 = manager.createTask(new Task("Дневная", "Описание",
                Duration.ofMinutes(120), LocalDateTime.of(2024, 1, 15, 14, 0)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(3, prioritized.length);


        assertEquals("Утренняя", prioritized[0].getName());
        assertEquals("Дневная", prioritized[1].getName());
        assertEquals("Вечерняя", prioritized[2].getName());
    }

    @Test
    @DisplayName("Задачи без времени находятся в конце списка")
    void testTasksWithoutTimeAtTheEnd() throws IOException, InterruptedException {
        Task taskWithTime = manager.createTask(new Task("С временем", "Описание",
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 15, 10, 0)));
        Task taskWithoutTime = manager.createTask(new Task("Без времени", "Описание"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length);
        assertEquals("С временем", prioritized[0].getName());
        assertEquals("Без времени", prioritized[1].getName());
    }

    @Test
    @DisplayName("Некорректный метод для приоритизированного списка (405)")
    void testInvalidMethodForPrioritized() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}