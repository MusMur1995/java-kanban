package javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import javakanban.manager.task.TaskManager;
import javakanban.models.Epic;
import javakanban.models.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends AbstractTaskHandler<Epic> {
    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson, Epic.class);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.matches("/epics/\\d+/subtasks")) {
            handleEpicSubtasks(exchange, path);
            return;
        }

        super.handle(exchange);
    }

    private void handleEpicSubtasks(HttpExchange exchange, String path) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendNotAllowed(exchange);
            return;
        }

        try {
            String[] pathParts = path.split("/");
            int epicId = Integer.parseInt(pathParts[2]);

            List<Subtask> subtasks = taskManager.getSubtasksByEpic(epicId);
            String jsonResponse = gson.toJson(subtasks);
            sendText(exchange, jsonResponse);

        } catch (IllegalArgumentException e) {
            System.out.println("Эпик не найден: " + e.getMessage());
            sendNotFound(exchange);
        } catch (Exception exception) {
            System.out.println("Внутренняя ошибка: " + exception.getMessage());
            exception.printStackTrace();
            sendInternalError(exchange);
        }
    }

    @Override
    protected List<Epic> getAllEntities() {
        return taskManager.getAllEpics();
    }

    @Override
    protected Epic getEntityById(int id) {
        return taskManager.getEpicById(id);
    }

    @Override
    protected Epic createEntity(Epic entity) {
        return taskManager.createEpic(entity);
    }

    @Override
    protected Epic updateEntity(Epic entity) {
        return taskManager.updateEpic(entity);
    }

    @Override
    protected void deleteEntity(int id) {
        taskManager.deleteEpicById(id);
    }

    @Override
    protected void setEntityId(Epic entity, int id) {
        entity.setId(id);
    }

    @Override
    protected String getBasePath() {
        return "/epics";
    }
}