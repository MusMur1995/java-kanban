package javakanban.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import javakanban.manager.Managers;
import javakanban.manager.task.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private HttpServer server;
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = createGson();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        configureHandlers();
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .serializeNulls()
                .create();
    }

    private void configureHandlers() {
        TasksHandler tasksHandler = new TasksHandler(taskManager, gson);
        SubtasksHandler subtasksHandler = new SubtasksHandler(taskManager, gson);
        EpicsHandler epicsHandler = new EpicsHandler(taskManager, gson);
        HistoryHandler historyHandler = new HistoryHandler(taskManager, gson);
        PrioritizedHandler prioritizedHandler = new PrioritizedHandler(taskManager, gson);

        server.createContext("/tasks/", tasksHandler);
        server.createContext("/subtasks/", subtasksHandler);
        server.createContext("/epics/", epicsHandler);

        server.createContext("/tasks", tasksHandler);
        server.createContext("/subtasks", subtasksHandler);
        server.createContext("/epics", epicsHandler);

        server.createContext("/history", historyHandler);
        server.createContext("/prioritized", prioritizedHandler);
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public Gson getGson() {
        return gson;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();

        System.out.println("Нажмите Enter для остановки сервера...");
        System.in.read();
        taskServer.stop();
    }
}