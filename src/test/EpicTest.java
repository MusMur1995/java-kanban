import javakanban.models.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    //проверьте, что наследники класса Task (Epic) равны друг другу, если равен их id;
    @Test
    @DisplayName("Эпики с одинаковым ID должны быть равны")
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        epic1.setId(100);

        Epic epic2 = new Epic("Epic 2", "Description 2");
        epic2.setId(100); // Устанавливаем такой же id

        System.out.println("Epic 1: ID=" + epic1.getId() + ", hash=" + epic1.hashCode());
        System.out.println("Epic 2: ID=" + epic2.getId() + ", hash=" + epic2.hashCode());

        assertEquals(epic1, epic2, "Эпики с одинаковыми id должны быть равны");
        assertEquals(epic1.hashCode(), epic2.hashCode(),
                "Хэш-коды эпиков с одинаковыми id должны совпадать");
    }

    //проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    @Test
    @DisplayName("Epic не должен ссылаться сам на себя как на подзадачу")
    void epicCannotContainItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Epic Description");
        epic.setId(1);

        epic.addSubtaskId(epic.getId());

        assertFalse(epic.getSubtaskIds().contains(epic.getId()),
                "Эпик не должен содержать свой собственный ID как подзадачу");
    }

    @Test
    @DisplayName("Epic должен корректно добавлять ID подзадач")
    void epicShouldAddSubtaskIdsCorrectly() {
        Epic epic = new Epic("Epic", "Epic Description");
        epic.setId(1);

        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        assertEquals(2, epic.getSubtaskIds().size(), "Ожидается 2 подзадачи в списке");
        assertTrue(epic.getSubtaskIds().contains(2));
        assertTrue(epic.getSubtaskIds().contains(3));
    }
}