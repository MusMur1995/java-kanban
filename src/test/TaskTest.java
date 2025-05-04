import javakanban.models.Epic;
import javakanban.models.Subtask;
import javakanban.models.Task;
import javakanban.models.TaskStatus;
import javakanban.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TaskTest {

    //проверьте, что экземпляры класса Task равны друг другу, если равен их id;
    @Test
    @DisplayName("Проверка равенства задач")
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Different description");

        task1.setId(1);
        task2.setId(1);

        System.out.println("Task 1: ID=" + task1.getId() + ", hash=" + task1.hashCode());
        System.out.println("Task 2: ID=" + task2.getId() + ", hash=" + task2.hashCode());

        assertEquals(task1, task2, "Задачи с одинаковыми ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хеш-коды должны совпадать");
    }

    @Test
    @DisplayName("Проверка неравенства эпика и подзадачи с одинаковым ID")
    void epicAndSubtaskWithSameIdShouldNotBeEqual() {
        Task epic = new Epic("Epic", "Description");
        epic.setId(200);

        Task subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setId(200); // Устанавливаем такой же id

        assertNotEquals(epic, subtask,
                "Эпик и подзадача не должны быть равны, даже с одинаковыми id");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    @DisplayName("Задачи с заданным и сгенерированным id не должны конфликтовать")
    void tasksWithManualAndGeneratedIdsShouldNotConflict() {
        TaskManager manager = Managers.getDefault();

        Task manualTask = new Task("Manual", "Set ID");
        manualTask.setId(10);
        manager.createTask(manualTask);

        Task generatedTask = new Task("Generated", "Auto ID");
        manager.createTask(generatedTask);

        System.out.println("Task 1: ID=" + manualTask.getId() + ", hash=" + manualTask.hashCode());
        System.out.println("Task 2: ID=" + generatedTask.getId() + ", hash=" + generatedTask.hashCode());

        assertNotEquals(manualTask.getId(), generatedTask.getId());
    }

    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    @DisplayName("Задача должна сохранять все поля при добавлении в менеджер")
    void taskShouldRemainUnchangedAfterAddition() {
        TaskManager manager = Managers.getDefault();

        Task task = new Task("Immutable", "Do not mutate");
        task.setStatus(TaskStatus.IN_PROGRESS);

        Task createdTask = manager.createTask(task);

        Task retrieved = manager.getTaskById(createdTask.getId());

        assertEquals(task.getName(), retrieved.getName(), "Имя должно совпадать");
        assertEquals(task.getDescription(), retrieved.getDescription(), "Описание должно совпадать");
        assertEquals(task.getStatus(), retrieved.getStatus(), "Статус должен совпадать");
        assertEquals(createdTask.getId(), retrieved.getId(), "ID должен совпадать");
    }

    @Test
    @DisplayName("Task должен корректно сохранять name, description и статус по умолчанию")
    void constructorShouldInitializeFieldsCorrectly() {
        Task task = new Task("Read book", "Finish reading chapter 3");

        assertEquals("Read book", task.getName());
        assertEquals("Finish reading chapter 3", task.getDescription());
        assertEquals(TaskStatus.NEW, task.getStatus(), "Статус должен быть NEW по умолчанию");
    }
}