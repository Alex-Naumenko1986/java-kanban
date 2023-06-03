package service.history;

import model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private HashMap<Integer, Node<Task>> idToNode;
    private Node<Task> head;
    private Node<Task> tail;

    public InMemoryHistoryManager() {
        head = null;
        tail = null;
        idToNode = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        removeTaskById(task.getId());
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        removeTaskById(id);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(tail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        }
        else {
            oldTail.next = newNode;
        }
        int id = newNode.data.getId();
        idToNode.remove(id);
        idToNode.put(id, newNode);
    }

    private List<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node<Task> node = head;
        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }
        return tasks;
    }

    private boolean removeTaskById(int id) {
        Node<Task> node = idToNode.get(id);
        if (node == null) {
            return false;
        }
        removeNode(node);
        idToNode.remove(id);
        return true;
    }

    private void removeNode(Node<Task> node) {
        final Node<Task> nextNode = node.next;
        final Node<Task> prevNode = node.prev;
        node.data = null;

        if (prevNode == null) {
            head = nextNode;
        } else {
            node.prev = null;
            prevNode.next = nextNode;
        }

        if (nextNode == null) {
            tail = prevNode;
        } else {
            node.next = null;
            nextNode.prev = prevNode;
        }
    }
}


