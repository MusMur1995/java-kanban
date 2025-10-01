package javakanban.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds = new ArrayList<>();

    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        this.endTime = null;
    }

    @Override
    public Duration getDuration() {
        return super.getDuration();
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        if (endTime != null) {
            return endTime;
        }
        return super.getEndTime();
    }

    public void setCalculatedTimes(LocalDateTime startTime, LocalDateTime endTime, Duration duration) {
        super.setStartTime(startTime);
        this.endTime = endTime;
        super.setDuration(duration);
    }

    @Override
    public void setDuration(Duration duration) {
        throw new UnsupportedOperationException("Duration эпика рассчитывается автоматически из подзадач");
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        throw new UnsupportedOperationException("StartTime эпика рассчитывается автоматически из подзадач");
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

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(this.getName(), this.getDescription(), this.getDuration(), this.getStartTime());
        copy.setId(this.getId());
        copy.setStatus(this.getStatus());
        copy.subtaskIds = new ArrayList<>(this.subtaskIds);
        copy.endTime = this.endTime;
        return copy;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() + "min" : "null") +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", subtasks=" + subtaskIds.size() +
                '}';
    }
}