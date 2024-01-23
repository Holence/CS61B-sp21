package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    private class Node {
        public T value;
        public Node prev;
        public Node next;

        public Node() {
            value = null;
            prev = this;
            next = this;
        }

        public Node(T x, Node prev, Node next) {
            this.value = x;
            this.prev = prev;
            this.next = next;
        }
    }

    private int size;
    private Node sentinel;

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node();
    }

    @Override
    public void addFirst(T item) {
        size += 1;
        Node first = new Node(item, sentinel, sentinel.next);
        sentinel.next.prev = first;
        sentinel.next = first;
    }

    @Override
    public void addLast(T item) {
        size += 1;
        Node last = new Node(item, sentinel.prev, sentinel);
        sentinel.prev.next = last;
        sentinel.prev = last;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.value + " ");
            p = p.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node first = sentinel.next;
        sentinel.next = first.next;
        first.next.prev = sentinel;
        size -= 1;
        return first.value;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node last = sentinel.prev;
        sentinel.prev = last.prev;
        last.prev.next = sentinel;
        size -= 1;
        return last.value;
    }

    @Override
    public T get(int index) {
        if (index < 0) {
            return null;
        }
        Node p = sentinel.next;
        for (int i = 0; i < index; i++) {
            if (p == sentinel) {
                return null;
            }
            p = p.next;
        }
        return p.value;
    }

    private T recursiveHelper(Node p, int index) {
        if (index == 0) {
            return p.value;
        }
        if (p == sentinel) {
            return null;
        }
        return recursiveHelper(p.next, index - 1);
    }

    public T getRecursive(int index) {
        if (index < 0) {
            return null;
        }
        return recursiveHelper(sentinel.next, index);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node p = sentinel.next;

            @Override
            public boolean hasNext() {
                return p != sentinel;
            }

            @Override
            public T next() {
                Node prev = p;
                p = p.next;
                return prev.value;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Deque) {
            Deque alias = (Deque) obj;
            if (size() != alias.size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if (!get(i).equals(alias.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
