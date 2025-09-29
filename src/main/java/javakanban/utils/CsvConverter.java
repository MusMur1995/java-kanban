package javakanban.utils;

import javakanban.models.*;

public class CsvConverter {

    private CsvConverter() {
    }

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }

    public static String toString(Task task) {
        return String.format("%d,TASK,%s,%s,%s,",
                task.getId(),
                task.getName(),
                task.getStatus(),
                task.getDescription());
    }

    public static String toString(Epic epic) {
        return String.format("%d,EPIC,%s,%s,%s,",
                epic.getId(),
                epic.getName(),
                epic.getStatus(),
                epic.getDescription());
    }

    public static String toString(Subtask subtask) {
        return String.format("%d,SUBTASK,%s,%s,%s,%d",
                subtask.getId(),
                subtask.getName(),
                subtask.getStatus(),
                subtask.getDescription(),
                subtask.getEpicId());
    }

    public static Task fromString(String value) {
        String[] fields = value.split(",");

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;

            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;

            case SUBTASK:
                int epicId = fields.length > 5 && !fields[5].isEmpty() ? Integer.parseInt(fields[5]) : -1;
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;

            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}