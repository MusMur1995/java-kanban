package javakanban.utils;

import javakanban.models.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class CsvConverter {

    public static String getHeader() {
        return "id,type,name,status,description,duration,startTime,epic";
    }

    public static String toString(Task task) {
        return String.format("%d,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                formatDuration(task.getDuration()),
                formatDateTime(task.getStartTime()));
    }

    public static String toString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s,%s,%s",
                epic.getId(),
                epic.getType(),
                epic.getName(),
                epic.getStatus(),
                epic.getDescription(),
                formatDuration(epic.getDuration()),
                formatDateTime(epic.getStartTime()));
    }

    public static String toString(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%d",
                subtask.getId(),
                subtask.getType(),
                subtask.getName(),
                subtask.getStatus(),
                subtask.getDescription(),
                formatDuration(subtask.getDuration()),
                formatDateTime(subtask.getStartTime()),
                subtask.getEpicId());
    }

    public static Task fromString(String value) {
        String[] fields = value.split(",", -1);

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        Duration duration = parseDuration(fields[5]);
        LocalDateTime startTime = parseDateTime(fields[6]);

        switch (type) {
            case TASK:
                Task task = new Task(name, description, duration, startTime);
                task.setId(id);
                task.setStatus(status);
                return task;

            case EPIC:
                Epic epic = new Epic(name, description, duration, startTime);
                epic.setId(id);
                epic.setStatus(status);
                return epic;

            case SUBTASK:
                int epicId = fields.length > 7 && !fields[7].isEmpty() ? Integer.parseInt(fields[7]) : -1;
                Subtask subtask = new Subtask(name, description, epicId, duration, startTime);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;

            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private static String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "";
        }
        return String.valueOf(duration.toMinutes());
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.toString();
    }

    private static Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ZERO;
        }
        return Duration.ofMinutes(Long.parseLong(durationStr));
    }

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr);
    }
}