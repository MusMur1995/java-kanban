package javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javakanban.manager.task.TaskManager;
import javakanban.models.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendNotAllowed(exchange);
                return;
            }
            List<Task> history = taskManager.getHistory();
            String jsonResponse = gson.toJson(history);
            sendText(exchange, jsonResponse);
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }
}
