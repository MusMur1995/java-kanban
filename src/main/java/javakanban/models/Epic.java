package javakanban.models;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public void addSubtaskId(int subtaskId) {
        if (this.getId() == subtaskId) {
            return;
        }
        subtaskIds.add(subtaskId);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(this.getName(), this.getDescription());
        copy.setId(this.getId());
        copy.setStatus(this.getStatus());
        copy.subtaskIds = new ArrayList<>(this.subtaskIds);
        return copy;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", subtasks=" + subtaskIds.size() +
                '}';
    }
}
