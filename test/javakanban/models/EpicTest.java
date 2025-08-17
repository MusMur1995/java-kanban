package javakanban.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    //проверьте, что наследники класса Task (Epic) равны друг другу, если равен их id;
    @Test
    @DisplayName("equals() должен вернуть true, если ID эпиков одинаковы")
    void equals_returnTrue_idsSame() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        epic1.setId(100);

        Epic epic2 = new Epic("Epic 2", "Description 2");
        epic2.setId(100);

        assertTrue(epic1.equals(epic2), "Эпики с одинаковыми id должны быть равны");
    }

    @Test
    @DisplayName("hashCode() должен быть одинаковым, если ID эпиков одинаковы")
    void hashCode_returnTrue_idsSame() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        epic1.setId(100);

        Epic epic2 = new Epic("Epic 2", "Description 2");
        epic2.setId(100);

        assertEquals(epic1.hashCode(), epic2.hashCode(), "Хэш-коды эпиков с одинаковыми id не совпадают");
    }

    //проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    @Test
    @DisplayName("addSubtaskId() не должен добавлять ID самого эпика")
    void addSubtaskId_doesNotAddSelfId() {
        Epic epic = new Epic("Epic", "Epic Description");
        epic.setId(1);

        epic.addSubtaskId(epic.getId());

        assertFalse(epic.getSubtaskIds().contains(epic.getId()),
                "Эпик не должен содержать свой собственный ID как подзадачу");
    }

    @Test
    @DisplayName("addSubtaskId() должен добавлять ID подзадачи в список")
    void addSubtaskId_addsSubtaskIdsToList() {
        Epic epic = new Epic("Epic", "Epic Description");
        epic.setId(1);

        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(2, subtaskIds.size(), "Ожидается 2 подзадачи в списке");
        assertTrue(epic.getSubtaskIds().contains(2));
        assertTrue(epic.getSubtaskIds().contains(3));
    }
}