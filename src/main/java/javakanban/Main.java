package javakanban;

import javakanban.manager.Managers;
import javakanban.models.*;
import javakanban.manager.task.TaskManager;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        // Создаем две задачи
        Task task1 = taskManager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description 2"));

        // Создаем эпик с тремя подзадачами
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Epic description 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Sub description", epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Sub description", epic1.getId()));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Subtask 3", "Sub description", epic1.getId()));

        // Создаем эпик без подзадач
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Epic description 2"));

        //Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться
        //taskManager.deleteTaskById(task2.getId());

        // Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        //taskManager.deleteEpicById(epic1.getId());

        // Получаем задачи для истории
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getSubtaskById(subtask3.getId());
        taskManager.getEpicById(epic2.getId());



        // Выводим историю
        printAllTasks(taskManager);

    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nВсе задачи:");
        System.out.println("Обычные задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("\nЭпики:");
        manager.getAllEpics().forEach(epic -> {
            System.out.println(epic);
            manager.getSubtasksByEpic(epic.getId()).forEach(subtask ->
                    System.out.println("--> " + subtask));
        });

        System.out.println("\nПодзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        System.out.println("\nИстория:");
        manager.getHistory().forEach(System.out::println);
    }

}