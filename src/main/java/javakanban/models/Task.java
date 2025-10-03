package javakanban.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Integer id;
    private TaskStatus status;

    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.id = -1;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public Task(String name, String description, Duration duration, LocalDateTime startTime) {
        this(name, description);
        this.duration = duration != null ? duration : Duration.ZERO;
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id != null ? id : -1;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration != null ? duration : Duration.ZERO;
    }

    public void setDuration(Duration duration) {
        this.duration = duration != null ? duration : Duration.ZERO;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public Task copy() {
        Task copy = new Task(this.name, this.description, this.duration, this.startTime);
        if (this.id != null) {
            copy.setId(this.id);
        }
        copy.setStatus(this.status);
        return copy;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration != null ? duration.toMinutes() + "min" : "null") +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}