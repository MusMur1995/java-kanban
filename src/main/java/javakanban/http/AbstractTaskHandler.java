package javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javakanban.exceptions.NotFoundException;
import javakanban.manager.task.TaskManager;

import java.io.IOException;
import java.util.List;

public abstract class AbstractTaskHandler<T> extends BaseHttpHandler implements HttpHandler {

    protected final Class<T> entityType;

    public AbstractTaskHandler(TaskManager taskManager, Gson gson, Class<T> entityType) {
        super(taskManager, gson);
        this.entityType = entityType;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String basePath = getBasePath();

        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path, basePath);
                case "POST" -> handlePost(exchange, path, basePath);
                case "DELETE" -> handleDelete(exchange, path, basePath);
                default -> sendNotAllowed(exchange);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("пересекается по времени")) {
                System.out.println("Обнаружено пересечение задач: " + e.getMessage());
                sendHasInteractions(exchange);
            } else {
                sendBadRequest(exchange, e.getMessage());
            }
        } catch (Exception exception) {
            handleException(exchange, method, path, exception);
        }
    }

    private void handleGet(HttpExchange exchange, String path, String basePath) throws IOException {
        if (path.equals(basePath)) {
            handleGetAll(exchange);
        } else if (path.startsWith(basePath + "/")) {
            handleGetById(exchange, path, basePath);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<T> entities = getAllEntities();
        String jsonResponse = gson.toJson(entities);
        sendText(exchange, jsonResponse);
    }

    private void handleGetById(HttpExchange exchange, String path, String basePath) throws IOException {
        int id = extractIdFromPath(path, basePath);
        T entity = getEntityById(id);
        String jsonResponse = gson.toJson(entity);
        sendText(exchange, jsonResponse);
    }

    private void handlePost(HttpExchange exchange, String path, String basePath) throws IOException {
        if (path.equals(basePath)) {
            handleCreate(exchange);
        } else if (path.startsWith(basePath + "/")) {
            handleUpdate(exchange, path, basePath);
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String bodyJson = readRequestBody(exchange);
        T newEntity = gson.fromJson(bodyJson, entityType);
        T createdEntity = createEntity(newEntity);
        String jsonResponse = gson.toJson(createdEntity);
        sendText(exchange, jsonResponse, 201);
    }

    private void handleUpdate(HttpExchange exchange, String path, String basePath) throws IOException {
        int id = extractIdFromPath(path, basePath);
        String bodyJson = readRequestBody(exchange);
        T updatedEntity = gson.fromJson(bodyJson, entityType);
        setEntityId(updatedEntity, id);
        T resultEntity = updateEntity(updatedEntity);
        String jsonResponse = gson.toJson(resultEntity);
        sendText(exchange, jsonResponse);
    }

    private void handleDelete(HttpExchange exchange, String path, String basePath) throws IOException {
        if (path.startsWith(basePath + "/")) {
            int id = extractIdFromPath(path, basePath);
            deleteEntity(id);
            sendText(exchange, "Задача с id " + id + " успешно удалена");
        } else if (path.equals(basePath)) {
            sendNotAllowed(exchange);
        }
    }

    private void handleException(HttpExchange exchange, String method, String path, Exception exception) throws IOException {
        String errorMessage = String.format(
                "ОШИБКА В ОБРАБОТЧИКЕ | Метод: %s | Путь: %s | Исключение: %s | Сообщение: %s",
                method, path, exception.getClass().getName(), exception.getMessage()
        );
        System.out.println(errorMessage);
        exception.printStackTrace();
        sendInternalError(exchange);
    }

    private int extractIdFromPath(String path, String basePath) {
        String idStr = path.substring(basePath.length() + 1);
        return Integer.parseInt(idStr);
    }

    protected abstract List<T> getAllEntities();

    protected abstract T getEntityById(int id);

    protected abstract T createEntity(T entity);

    protected abstract T updateEntity(T entity);

    protected abstract void deleteEntity(int id);

    protected abstract void setEntityId(T entity, int id);

    protected abstract String getBasePath();
}