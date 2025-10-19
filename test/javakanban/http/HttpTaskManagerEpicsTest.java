package javakanban.http;

import javakanban.exceptions.NotFoundException;
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

class HttpTaskManagerEpicsTest extends HttpBaseTest{

    @Test
    @DisplayName("Создание нового эпика через HTTP POST")
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getAllEpics();
        assertNotNull(epicsFromManager);
        assertEquals(1, epicsFromManager.size());
        assertEquals("Эпик 1", epicsFromManager.get(0).getName());
    }

    @Test
    @DisplayName("Получение всех эпиков через HTTP GET")
    void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Тестовый эпик", "Описание"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(1, epics.length);
        assertEquals("Тестовый эпик", epics[0].getName());
    }

    @Test
    @DisplayName("Получение эпика по ID через HTTP GET")
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        int epicId = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epicId, responseEpic.getId());
        assertEquals("Эпик", responseEpic.getName());
    }

    @Test
    @DisplayName("Попытка получения несуществующего эпика (404)")
    void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("Обновление эпика через HTTP POST")
    void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Оригинальный", "Описание"));
        Epic updatedEpic = new Epic("Обновленный", "Новое описание");
        updatedEpic.setId(epic.getId());
        String epicJson = gson.toJson(updatedEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic epicFromManager = manager.getEpicById(epic.getId());
        assertEquals("Обновленный", epicFromManager.getName());
    }

    @Test
    @DisplayName("Удаление эпика через HTTP DELETE")
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик для удаления", "Описание"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> manager.getEpicById(epic.getId()));
    }

    @Test
    @DisplayName("Получение подзадач эпика через HTTP GET")
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик с подзадачами", "Описание"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
        assertEquals("Подзадача 1", subtasks[0].getName());
        assertEquals("Подзадача 2", subtasks[1].getName());
    }

    @Test
    @DisplayName("Получение подзадач несуществующего эпика (404)")
    void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("Удаление эпика с подзадачами")
    void testDeleteEpicWithSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> manager.getEpicById(epic.getId()));
        assertThrows(NotFoundException.class, () -> manager.getSubtaskById(subtask.getId()));
    }

    @Test
    @DisplayName("Обновление статуса эпика через подзадачи")
    void testEpicStatusUpdate() throws IOException, InterruptedException {
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
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic epicFromManager = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, epicFromManager.getStatus());
    }

    @Test
    @DisplayName("Создание эпика с пустым списком подзадач")
    void testCreateEpicWithEmptySubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик без подзадач", "Описание");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Epic epicFromManager = manager.getEpicById(1);
        assertNotNull(epicFromManager.getSubtaskIds());
        assertTrue(epicFromManager.getSubtaskIds().isEmpty());
    }

    @Test
    @DisplayName("Некорректный метод для эндпоинта эпиков (405)")
    void testInvalidMethodForEpics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }

    @Test
    @DisplayName("Некорректный метод для подзадач эпика (405)")
    void testInvalidMethodForEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}