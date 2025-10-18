package javakanban.http;

import com.google.gson.Gson;
import javakanban.manager.Managers;
import javakanban.manager.task.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.http.HttpClient;

public abstract class HttpBaseTest {
    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected Gson gson;
    protected final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }
}