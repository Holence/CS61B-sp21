package deque;

import java.util.Iterator;

public interface Deque<ElemType> {
    public void addFirst(ElemType item);

    public void addLast(ElemType item);

    default public boolean isEmpty() {
        return size() == 0;
    }

    public int size();

    public void printDeque();

    // If no such item exists, returns null.
    public ElemType removeFirst();

    // If no such item exists, returns null.
    public ElemType removeLast();

    // If no such item exists, returns null.
    public ElemType get(int index);

    public Iterator<ElemType> iterator();

    public boolean equals(Object obj);
}
