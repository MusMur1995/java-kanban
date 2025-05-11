package javakanban.manager.history;

import javakanban.models.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.remove(0);
        }
        history.add(task.copy());
    }

    @Override
    public List<Task> getHistory() {
        List<Task> copiedHistory = new ArrayList<>();
        for (Task task : history) {
            copiedHistory.add(task.copy());
        }
        return copiedHistory;
    }
}