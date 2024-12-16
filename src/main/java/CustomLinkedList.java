public class CustomLinkedList<T> {
    private Node<T> head;
    private int size;

    // Each node holds data and points to the next node
    private static class Node<T> {
        T data;
        Node<T> next;

        // Creates a new node with the given data
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    // Creates an empty list
    public CustomLinkedList() {
        head = null;
        size = 0;
    }

    // Adds a new item to the end of the list
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    // Returns how many items are in the list
    public int size() {
        return size;
    }

    // Gets an item at a specific position
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    // Removes an item at a specific position
    public void remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        if (index == 0) {
            head = head.next;
        } else {
            Node<T> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            current.next = current.next.next;
        }
        size--;
    }
}
