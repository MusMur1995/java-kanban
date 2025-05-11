package javakanban.manager;

import javakanban.manager.history.HistoryManager;
import javakanban.manager.history.InMemoryHistoryManager;
import javakanban.manager.task.InMemoryTaskManager;
import javakanban.manager.task.TaskManager;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}