public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        System.out.println("=== ПОЛНОЕ ТЕСТИРОВАНИЕ TASKMANAGER ===");

        // 1. Тестирование методов для Task
        System.out.println("\n===== ТЕСТ МЕТОДОВ ДЛЯ TASK =====");
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        System.out.println("-- getAllTasks() --");
        System.out.println(manager.getAllTasks()); // [Task1, Task2]

        System.out.println("\n-- getTaskById() --");
        System.out.println(manager.getTaskById(task1.getId())); // Task1

        System.out.println("\n-- updateTask() --");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);
        System.out.println(manager.getTaskById(task1.getId())); // Проверяем обновление

        System.out.println("\n-- deleteTaskById() --");
        manager.deleteTaskById(task2.getId());
        System.out.println("Осталось задач: " + manager.getAllTasks().size()); // 1

        System.out.println("\n-- deleteAllTasks() --");
        manager.deleteAllTasks();
        System.out.println("Осталось задач: " + manager.getAllTasks().size()); // 0

        // 2. Тестирование методов для Epic
        System.out.println("\n===== ТЕСТ МЕТОДОВ ДЛЯ EPIC =====");
        Epic epic1 = manager.createEpic(new Epic("Epic 1", ""));
        Epic epic2 = manager.createEpic(new Epic("Epic 2", ""));

        System.out.println("-- getAllEpics() --");
        System.out.println(manager.getAllEpics()); // [Epic1, Epic2]

        System.out.println("\n-- getEpicById() --");
        System.out.println(manager.getEpicById(epic1.getId())); // Epic1

        System.out.println("\n-- updateEpic() --");
        epic1.setDescription("New description");
        manager.updateEpic(epic1);
        System.out.println(manager.getEpicById(epic1.getId())); // Проверяем обновление

        System.out.println("\n-- deleteEpicById() --");
        manager.deleteEpicById(epic2.getId());
        System.out.println("Осталось эпиков: " + manager.getAllEpics().size()); // 1

        System.out.println("\n-- deleteAllEpics() --");
        manager.deleteAllEpics();
        System.out.println("Осталось эпиков: " + manager.getAllEpics().size()); // 0

        // 3. Тестирование методов для Subtask
        System.out.println("\n===== ТЕСТ МЕТОДОВ ДЛЯ SUBTASK =====");
        Epic epic = manager.createEpic(new Epic("Main Epic", ""));
        Epic epic3 = manager.createEpic(new Epic("Second Epic", ""));
        Subtask sub1 = manager.createSubtask(new Subtask("Subtask 1", "", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Subtask 2", "", epic.getId()));
        Subtask sub3 = manager.createSubtask(new Subtask("Subtask 3", "", epic3.getId()));

        System.out.println("-- getAllSubtasks() --");
        System.out.println(manager.getAllSubtasks()); // [Subtask1, Subtask2]

        System.out.println("\n-- getSubtaskById() --");
        System.out.println(manager.getSubtaskById(sub1.getId())); // Subtask1

        System.out.println("\n-- getSubtasksByEpic() --");
        System.out.println(manager.getSubtasksByEpic(epic3.getId())); // Должны быть 2 подзадачи

        System.out.println("\n-- updateSubtask() --");
        sub1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        System.out.println("Статус эпика: " + epic.getStatus()); // Должен быть IN_PROGRESS

        System.out.println("\n-- deleteSubtaskById() --");
        manager.deleteSubtaskById(sub2.getId());
        System.out.println("Осталось подзадач: " + manager.getAllSubtasks().size()); // 1

        System.out.println("\n-- deleteAllSubtasks() --");
        manager.deleteAllSubtasks();
        System.out.println("Осталось подзадач: " + manager.getAllSubtasks().size()); // 0
        System.out.println("Статус эпика: " + epic.getStatus()); // Должен быть NEW
    }
}