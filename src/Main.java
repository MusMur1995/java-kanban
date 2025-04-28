import enums.TaskStatus;
import models.Epic;
import models.Subtask;
import models.Task;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.createTask(new Task("Забрать посылку", "До 14 дней")); // Создайте две задачи - задача 1
        Task task2 = taskManager.createTask(new Task("Запись к врачу", "Стоматолог, 25 мая")); // Создайте две задачи - задача 2

        Epic epic1 = taskManager.createEpic(new Epic("Переезд офиса", "Подготовка к переезду")); // эпик с двумя подзадачами

        Subtask subtask1 = taskManager.createSubtask(new Subtask("Купить коробки", "20 штук", epic1.getId())); // эпик 1, подзадача 1
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Нанять грузчиков", "Недорогие грузчики", epic1.getId())); // эпик 1, подзадача 2

        Epic epic2 = taskManager.createEpic(new Epic("Подготовка к экзамену", "Математика")); // эпик 2 с одной подзадачей

        Subtask subtask3 = taskManager.createSubtask(new Subtask("Прочитать главу 5", "Стр. 45-60", epic2.getId())); // эпик 2, подзадача 1

        System.out.println("=== Тестирование трекера задач ===");

        System.out.println("\n===Все задачи:===");
        System.out.println("\nОбычные задачи:");
        taskManager.getAllTasks().forEach(System.out::println);
        System.out.println("\nЭпики:");
        taskManager.getAllEpics().forEach(System.out::println);
        System.out.println("\nПодзадачи:");
        taskManager.getAllSubtasks().forEach(System.out::println);

        System.out.println("\n--- Статусы до изменений ---");
        System.out.println("Epic1 статус: " + epic1.getStatus() + " (должен быть NEW)");
        System.out.println("Epic2 статус: " + epic2.getStatus() + " (должен быть NEW)");

        task1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        subtask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask3);

        System.out.println("\n--- После изменения статусов ---");
        System.out.println("Task1 статус: " + task1.getStatus());
        System.out.println("Subtask1 статус: " + subtask1.getStatus());
        System.out.println("Subtask3 статус: " + subtask3.getStatus());
        System.out.println("Epic1 статус: " + epic1.getStatus());
        System.out.println("Epic2 статус: " + epic2.getStatus());

        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteEpicById(epic1.getId());

        System.out.println("\n--- После удаления ---");
        System.out.println("Осталось задач: " + taskManager.getAllTasks().size());
        System.out.println("Осталось эпиков: " + taskManager.getAllEpics().size() + " (должен быть 1)");
        System.out.println("Осталось подзадач: " + taskManager.getAllSubtasks().size() + " (должна быть 1)");
    }
}