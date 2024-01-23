package deque;

import java.util.Iterator;

public class LinkedListDeque<ElemType> implements Deque<ElemType>, Iterable<ElemType> {

    private class Node {
        public ElemType value;
        public Node prev;
        public Node next;

        public Node() {
            value = null;
            prev = this;
            next = this;
        }

        public Node(ElemType x, Node prev, Node next) {
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
    public void addFirst(ElemType item) {
        size += 1;
        Node first = new Node(item, sentinel, sentinel.next);
        sentinel.next.prev = first;
        sentinel.next = first;
    }

    @Override
    public void addLast(ElemType item) {
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
    public ElemType removeFirst() {
        if (isEmpty())
            return null;
        Node first = sentinel.next;
        sentinel.next = first.next;
        first.next.prev = sentinel;
        size -= 1;
        return first.value;
    }

    @Override
    public ElemType removeLast() {
        if (isEmpty())
            return null;
        Node last = sentinel.prev;
        sentinel.prev = last.prev;
        last.prev.next = sentinel;
        size -= 1;
        return last.value;
    }

    @Override
    public ElemType get(int index) {
        if (index < 0)
            return null;
        Node p = sentinel.next;
        for (int i = 0; i < index; i++) {
            if (p == sentinel)
                return null;
            p = p.next;
        }
        return p.value;
    }

    private ElemType recursiveHelper(Node p, int index) {
        if (index == 0)
            return p.value;
        if (p == sentinel)
            return null;
        return recursiveHelper(p.next, index - 1);
    }

    public ElemType getRecursive(int index) {
        if (index < 0)
            return null;
        return recursiveHelper(sentinel.next, index);
    }

    @Override
    public Iterator<ElemType> iterator() {
        return new Iterator<ElemType>() {
            private Node p = sentinel.next;

            @Override
            public boolean hasNext() {
                return p != sentinel;
            }

            @Override
            public ElemType next() {
                Node prev = p;
                p = p.next;
                return prev.value;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Deque alias) {
            if (size() != alias.size())
                return false;
            Iterator<ElemType> a = iterator();
            Iterator<ElemType> b = alias.iterator();
            while (a.hasNext()) {
                if (!a.next().equals(b.next()))
                    return false;
            }
            return true;
        }
        return false;
    }
}
