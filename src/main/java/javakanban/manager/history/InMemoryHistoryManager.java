package javakanban.manager.history;

import javakanban.models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер истории просмотров задач, реализующий:
 * 1. Хранение истории просмотров
 * 2. Удаление дубликатов
 * 3. Быстрый доступ к элементам истории
 */
public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> historyMap = new HashMap<>();

    private Node head;
    private Node tail;

    /**
     * Добавляет задачу в историю просмотров
     *
     * @param task задача для добавления (если null - игнорируется)
     */

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getId()); // Удаляем старую версию если есть

        Task taskCopy = task.copy();
        Node newNode = linkLast(taskCopy);
        historyMap.put(taskCopy.getId(), newNode);
    }

    /**
     * Удаляет задачу из истории по ID
     *
     * @param id идентификатор задачи для удаления
     */
    @Override
    public void remove(int id) {

        if (historyMap.containsKey(id)) {

            removeNode(historyMap.get(id));
        }
    }

    /**
     * Возвращает список всех задач в истории просмотров
     *
     * @return список задач в порядке просмотра (от старых к новым)
     */
    @Override
    public List<Task> getHistory() {
        // Создаем защитную копию для безопасности
        List<Task> result = new ArrayList<>();
        Node current = head;

        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }

    /**
     * Внутренний класс для узла двусвязного списка
     */
    private static class Node {
        Task task;
        Node prev;
        Node next;
    }

    /**
     * Добавляет задачу в конец двусвязного списка
     *
     * @param task задача для добавления
     */
    private Node linkLast(Task task) {

        Node newNode = new Node();
        newNode.task = task;
        newNode.prev = tail;
        newNode.next = null;

        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }

        tail = newNode;

        return newNode;
    }

    /**
     * Удаляет узел из двусвязного списка
     *
     * @param node узел для удаления
     */
    private void removeNode(Node node) {

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {

            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {

            tail = node.prev;
        }

        historyMap.remove(node.task.getId());
    }
}
