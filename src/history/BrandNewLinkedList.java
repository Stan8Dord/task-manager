package history;

import java.util.ArrayList;

public class BrandNewLinkedList<T> {
    protected Node<T> head;
    protected Node<T> tail;
    protected int size = 0;

    public void linkLast(T element) {
        final Node<T> oldTail = tail;
        final Node<T> newNode = new Node<>(element, oldTail, null);

        tail = newNode;
        if (oldTail == null)
            head = newNode;
        else
            oldTail.setNext(newNode);

        size++;
    }

    public ArrayList<T> getTasks() {
        ArrayList<T> tasks = new ArrayList<>();

        Node<T> node = head;
        while (node != null) {
            tasks.add(node.getData());
            node = node.getNext();
        }
        return tasks;
    }

    public void removeNode(Node<T> deadNode) {
        if (size != 0) {
            Node<T> node = deadNode.getPrev();
            if (node != null) {
                node.setNext(deadNode.getNext());
            } else {
                head = deadNode.getNext();
            }
            node = deadNode.getNext();
            if (node != null) {
                node.setPrev(deadNode.getPrev());
            } else {
                tail = deadNode.getPrev();
            }
            size--;
        }
    }
}
