package javakanban.manager;

import javakanban.manager.history.HistoryManager;
import javakanban.manager.task.TaskManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {
    //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
    @Test
    @DisplayName("getDefault() возвращает проинициализированный TaskManager")
    void getDefault_returnsInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "TaskManager должен быть проинициализирован");
    }

    @Test
    @DisplayName("getDefaultHistory() возвращает проинициализированный HistoryManager")
    void getDefaultHistory_returnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован");
    }
}