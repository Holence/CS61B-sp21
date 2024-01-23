package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {

    private T[] array;
    private int size;
    private int nextfirst, nextlast;
    private int MAX_SIZE = 8;

    public ArrayDeque() {
        array = (T[]) new Object[MAX_SIZE];
        size = 0;
        nextfirst = MAX_SIZE - 1;
        nextlast = 0;
    }

    private boolean full() {
        return size == array.length;
    }

    /** 是否太空 */
    private boolean hollow() {
        return MAX_SIZE > 16 && (float) size / MAX_SIZE < 0.25;
    }

    /** 方便计算真实下标 */
    private int truePos(int begin, int offset) {
        return (MAX_SIZE + begin + offset) % MAX_SIZE;
    }

    @Override
    public int size() {
        return size;
    }

    // 傻了，直接用get()从头到尾遍历一遍赋值到newArray就行了，哪里用得着分类讨论啊……
    // 只能说这是用System.arraycopy的方法……

    /** 正常顺序，填到newArray最左侧 */
    private void normalTypeResize(int newSize) {
        T[] newArray = (T[]) new Object[newSize];
        System.arraycopy(array, truePos(nextfirst, 1), newArray, 0, size);
        nextlast = size;
        array = newArray;
        nextfirst = array.length - 1;
    }

    /** 首在右，尾在左，分别把首尾填到newArray的两端 */
    private void abnormalTypeResize(int newSize) {
        // 中间分裂
        T[] newArray = (T[]) new Object[newSize];
        int rightNums = array.length - nextfirst - 1;
        int leftNums = size - rightNums;
        // 右边是开头的部分，填到newArray的尾部
        System.arraycopy(array, nextfirst + 1, newArray, newArray.length - rightNums, rightNums);
        // 左边是结尾的部分，填到newArray的头部
        System.arraycopy(array, 0, newArray, 0, leftNums);
        array = newArray;
        // nextfirst从后往前数
        nextfirst = newArray.length - rightNums - 1;
        // nextlast从前往后数
        nextlast = leftNums;
    }

    private void expand() {
        int newSize = MAX_SIZE * 2;
        if (nextfirst > nextlast) {
            normalTypeResize(newSize);
        } else {
            abnormalTypeResize(newSize);
        }
        MAX_SIZE = newSize;
    }

    private void shrink() {
        int newSize = MAX_SIZE / 4;
        if (nextlast > nextfirst) {
            normalTypeResize(newSize);
        } else {
            abnormalTypeResize(newSize);
        }
        MAX_SIZE = newSize;
    }

    @Override
    public void addFirst(T item) {
        if (full()) {
            expand();
        }
        size += 1;
        array[nextfirst] = item;

        nextfirst = truePos(nextfirst, -1);
    }

    @Override
    public void addLast(T item) {
        if (full()) {
            expand();
        }
        size += 1;
        array[nextlast] = item;

        nextlast = truePos(nextlast, 1);
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        if (hollow()) {
            shrink();
        }
        size -= 1;
        nextfirst = truePos(nextfirst, 1);
        T ret = array[nextfirst];
        array[nextfirst] = null;
        return ret;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        if (hollow()) {
            shrink();
        }
        size -= 1;
        nextlast = truePos(nextlast, -1);
        T ret = array[nextlast];
        array[nextlast] = null;
        return ret;
    }

    @Override
    public T get(int index) {
        if (index < 0) {
            return null;
        }
        return array[truePos(nextfirst + 1, index)];
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                return get(index++);
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
