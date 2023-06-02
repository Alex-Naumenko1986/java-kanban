package service.history;

import model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private CustomLinkedList<Task> taskHistory;

    public InMemoryHistoryManager() {
        taskHistory = new CustomLinkedList<>();
    }

    @Override
    public void add(Task task) {
        taskHistory.removeTaskById(task.getId());
        taskHistory.linkLast(task);
    }

    @Override
    public void remove(int id) {
        taskHistory.removeTaskById(id);
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getTasks();
    }
}

class CustomLinkedList<T extends Task> {
    private HashMap<Integer, Node<T>> idToNode = new HashMap<>();
    private Node<T> head;
    private Node<T> tail;

    public CustomLinkedList() {
        head = null;
        tail = null;
    }

    public void linkLast(T element) {
        final Node<T> oldTail = tail;
        final Node<T> newNode = new Node<>(tail, element, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        }
        else {
            oldTail.next = newNode;
        }
        idToNode.put(newNode.data.getId(), newNode);
    }

    public List<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node<T> node = head;
        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }
        return tasks;
    }

    public boolean removeTaskById(int id) {
        Node<T> node = idToNode.get(id);
        if (node == null) {
            return false;
        }
        removeNode(node);
        idToNode.remove(id);
        return true;
    }

    private void removeNode(Node<T> node) {
        final Node<T> nextNode = node.next;
        final Node<T> prevNode = node.prev;

        if (prevNode == null) {
            head = nextNode;
        } else {
            prevNode.next = nextNode;
            node.prev = null;
        }

        if (nextNode == null) {
            tail = prevNode;
        } else {
            nextNode.prev = prevNode;
            node.next = null;
        }
    }
}
