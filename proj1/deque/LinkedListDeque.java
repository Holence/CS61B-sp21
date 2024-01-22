package deque;

public class LinkedListDeque<ElemType> implements Deque<ElemType> {

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
        Node p = sentinel.next;
        for (int i = 0; i < index; i++) {
            if (p == sentinel)
                return null;
            p = p.next;
        }
        return p.value;
    }
}
