package service.history;

import model.Task;

public class Node<E extends Task>{
    public Node<E> prev;
    public E data;
    public Node<E> next;

    public Node(Node prev, E data, Node next) {
        this.prev = prev;
        this.data = data;
        this.next = next;
    }
}
