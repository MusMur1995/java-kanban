package javakanban.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    //проверьте, что наследники класса Task (Subtask) равны друг другу, если равен их id;
    @Test
    @DisplayName("equals() должен вернуть true, если ID подзадач одинаковы")
    void equals_returnTrue_idsSame() {
        Task subtask1 = new Subtask("Subtask 1", "Description 1", 1);
        subtask1.setId(100);

        Task subtask2 = new Subtask("Subtask 2", "Description 2", 1);
        subtask2.setId(100);

        assertTrue(subtask1.equals(subtask2), "Подзадачи с одинаковыми id должны быть равны");
    }

    @Test
    @DisplayName("hashCode() должен быть одинаковым, если ID подзадач одинаковы")
    void hashCode_returnTrue_idsSame() {
        Task subtask1 = new Subtask("Subtask 1", "Description 1", 1);
        subtask1.setId(100);

        Task subtask2 = new Subtask("Subtask 2", "Description 2", 1);
        subtask2.setId(100);

        assertEquals(subtask1.hashCode(), subtask2.hashCode(),
                "Хэш-коды подзадач с одинаковыми id должны совпадать");
    }

    //проверьте, что объект Subtask нельзя сделать своим же эпиком;
    @Test
    @DisplayName("Подзадача не может ссылаться на саму себя как на эпик")
    void subtask_cannotReferenceItselfAsEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", 1);
        subtask.setId(1);
        boolean referencesItself = subtask.getId() == subtask.getEpicId();
        assertFalse(referencesItself, "Подзадача не должна ссылаться на саму себя как на эпик");
    }

    @Test
    @DisplayName("Subtask должна корректно хранить epicId, если он не совпадает с её id")
    void subtask_storeEpicId_notEqualsId() {
        Subtask subtask = new Subtask("Subtask", "Description", 42);
        subtask.setId(10); // epicId ≠ id

        assertEquals(42, subtask.getEpicId(),
                "epicId должен сохраняться, если не равен id подзадачи");
    }

    @Test
    @DisplayName("Subtask должна сбрасывать epicId при совпадении с собственным id")
    void subtask_resetEpicId_ifEqualsId() {
        Subtask subtask = new Subtask("Subtask", "Description", 5);
        subtask.setId(5); // совпадает с epicId → должен сброситься

        assertEquals(-1, subtask.getEpicId(),
                "epicId должен сбрасываться, если совпадает с id подзадачи");
    }
}