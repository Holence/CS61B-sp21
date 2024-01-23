package deque;

import java.util.Iterator;

public interface Deque<T> {
    void addFirst(T item);

    void addLast(T item);

    default boolean isEmpty() {
        return size() == 0;
    }

    int size();

    void printDeque();

    // If no such item exists, returns null.
    T removeFirst();

    // If no such item exists, returns null.
    T removeLast();

    // If no such item exists, returns null.
    T get(int index);

    Iterator<T> iterator();

    boolean equals(Object obj);
}
