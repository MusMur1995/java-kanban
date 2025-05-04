import javakanban.service.HistoryManager;
import javakanban.service.Managers;
import javakanban.service.TaskManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {
    //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
    @Test
    @DisplayName("Утилитарный класс должен возвращать проинициализированные экземпляры менеджеров")
    void utilityClassShouldReturnInitializedManagers() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "TaskManager должен быть проинициализирован");
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован");
    }
}