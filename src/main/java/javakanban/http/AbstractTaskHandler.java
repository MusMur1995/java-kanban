package javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
                case "GET" -> {
                    if (path.equals(basePath)) {
                        List<T> entities = getAllEntities();
                        String jsonResponse = gson.toJson(entities);
                        sendText(exchange, jsonResponse);
                    } else if (path.startsWith(basePath + "/")) {
                        int id = extractIdFromPath(path, basePath);
                        T entity = getEntityById(id);
                        if (entity != null) {
                            String jsonResponse = gson.toJson(entity);
                            sendText(exchange, jsonResponse);
                        } else sendNotFound(exchange);
                    }
                }
                case "POST" -> {
                    if (path.equals(basePath)) {
                        String bodyJson = readRequestBody(exchange);
                        T newEntity = gson.fromJson(bodyJson, entityType);
                        T createdEntity = createEntity(newEntity);
                        String jsonResponse = gson.toJson(createdEntity);
                        sendText(exchange, jsonResponse, 201);
                    } else if (path.startsWith(basePath + "/")) {
                        int id = extractIdFromPath(path, basePath);
                        String bodyJson = readRequestBody(exchange);
                        T updatedEntity = gson.fromJson(bodyJson, entityType);
                        setEntityId(updatedEntity, id);
                        T resultEntity = updateEntity(updatedEntity);
                        String jsonResponse = gson.toJson(resultEntity);
                        sendText(exchange, jsonResponse);
                    }
                }
                case "DELETE" -> {
                    if (path.startsWith(basePath + "/")) {
                        int id = extractIdFromPath(path, basePath);
                        deleteEntity(id);
                        sendText(exchange, "Задача с id " + id + " успешно удалена");
                    } else if (path.equals(basePath)) {
                        sendNotAllowed(exchange);
                    }
                }
                default -> sendNotAllowed(exchange);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Обнаружено пересечение задач: " + e.getMessage());
            sendHasInteractions(exchange);
        } catch (Exception exception) {
            System.out.println("Метод: " + method);
            System.out.println("Путь: " + path);
            System.out.println("Исключение: " + exception.getClass().getName());
            System.out.println("Сообщение: " + exception.getMessage());
            exception.printStackTrace();
            sendInternalError(exchange);
        }
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
