package javakanban.http;
import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest extends HttpBaseTest{

    @Test
    @DisplayName("Создание подзадачи через HTTP POST")
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Родительский эпик", "Описание"));
        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getAllSubtasks();
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Подзадача", subtasksFromManager.get(0).getName());
    }

    @Test
    @DisplayName("Получение всех подзадач через HTTP GET")
    void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        manager.createSubtask(new Subtask("Подзадача 1", "Описание", epic.getId()));
        manager.createSubtask(new Subtask("Подзадача 2", "Описание", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    @DisplayName("Получение подзадачи по ID через HTTP GET")
    void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask responseSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), responseSubtask.getId());
        assertEquals("Подзадача", responseSubtask.getName());
    }

    @Test
    @DisplayName("Обновление подзадачи через HTTP POST")
    void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Оригинал", "Описание", epic.getId()));

        Subtask updatedSubtask = new Subtask("Обновленная", "Новое описание", epic.getId());
        updatedSubtask.setId(subtask.getId());
        updatedSubtask.setStatus(TaskStatus.IN_PROGRESS);
        String subtaskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask subtaskFromManager = manager.getSubtaskById(subtask.getId());
        assertEquals("Обновленная", subtaskFromManager.getName());
        assertEquals(TaskStatus.IN_PROGRESS, subtaskFromManager.getStatus());
    }

    @Test
    @DisplayName("Удаление подзадачи через HTTP DELETE")
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(manager.getSubtaskById(subtask.getId()));
    }

    @Test
    @DisplayName("Обновление статуса эпика при изменении подзадачи")
    void testEpicStatusUpdateFromSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        Subtask updatedSubtask = new Subtask("Подзадача", "Описание", epic.getId());
        updatedSubtask.setId(subtask.getId());
        updatedSubtask.setStatus(TaskStatus.DONE);
        String subtaskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    }
}