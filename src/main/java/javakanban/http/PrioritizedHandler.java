package javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javakanban.manager.task.TaskManager;
import javakanban.models.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendNotAllowed(exchange);
                return;
            }
            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
            String jsonResponse = gson.toJson(prioritizedTasks);
            sendText(exchange, jsonResponse);
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }
}