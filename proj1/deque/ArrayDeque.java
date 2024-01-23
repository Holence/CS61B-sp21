package deque;

import java.util.Iterator;

public class ArrayDeque<ElemType> implements Deque<ElemType> {

    private ElemType[] array;
    private int size;
    private int nextfirst, nextlast;
    private int MAX_SIZE = 8;

    public ArrayDeque() {
        array = (ElemType[]) new Object[MAX_SIZE];
        size = 0;
        nextfirst = MAX_SIZE - 1;
        nextlast = 0;
    }

    private boolean full() {
        return size == array.length;
    }

    /** 是否太空 */
    private boolean hollow() {
        return size > 16 && (float) size / MAX_SIZE < 0.25;
    }

    /** 方便计算真实下标 */
    private int truePos(int begin, int offset) {
        return (MAX_SIZE + begin + offset) % MAX_SIZE;
    }

    @Override
    public int size() {
        return size;
    }

    // 傻了，直接用get()从头到尾遍历一遍赋值到new_array就行了，哪里用得着分类讨论啊……
    // 只能说这是用System.arraycopy的方法……

    /** 正常顺序，填到new_array最左侧 */
    private void normalTypeResize(int new_size) {
        ElemType[] new_array = (ElemType[]) new Object[new_size];
        System.arraycopy(array, truePos(nextfirst, 1), new_array, 0, size);
        nextlast = size;
        array = new_array;
        nextfirst = array.length - 1;
    }

    /** 首在右，尾在左，分别把首尾填到new_array的两端 */
    private void abnormalTypeResize(int new_size) {
        // 中间分裂
        ElemType[] new_array = (ElemType[]) new Object[new_size];
        int right_nums = array.length - nextfirst - 1;
        int left_nums = size - right_nums;
        // 右边是开头的部分，填到new_array的尾部
        System.arraycopy(array, nextfirst + 1, new_array, new_array.length - right_nums, right_nums);
        // 左边是结尾的部分，填到new_array的头部
        System.arraycopy(array, 0, new_array, 0, left_nums);
        array = new_array;
        // nextfirst从后往前数
        nextfirst = new_array.length - right_nums - 1;
        // nextlast从前往后数
        nextlast = left_nums;
    }

    private void expand() {
        int new_size = MAX_SIZE * 2;
        if (nextfirst > nextlast) {
            normalTypeResize(new_size);
        } else {
            abnormalTypeResize(new_size);
        }
        MAX_SIZE = new_size;
    }

    private void shrink() {
        int new_size = MAX_SIZE / 2;
        if (nextlast > nextfirst) {
            normalTypeResize(new_size);
        } else {
            abnormalTypeResize(new_size);
        }
        MAX_SIZE = new_size;
    }

    @Override
    public void addFirst(ElemType item) {
        if (full()) {
            expand();
        }
        size += 1;
        array[nextfirst] = item;

        nextfirst = truePos(nextfirst, -1);
    }

    @Override
    public void addLast(ElemType item) {
        if (full()) {
            expand();
        }
        size += 1;
        array[nextlast] = item;

        nextlast = truePos(nextlast, 1);
    }

    @Override
    public ElemType removeFirst() {
        if (isEmpty()) {
            return null;
        }

        if (hollow()) {
            shrink();
        }
        size -= 1;
        nextfirst = truePos(nextfirst, 1);
        ElemType ret = array[nextfirst];
        array[nextfirst] = null;
        return ret;
    }

    @Override
    public ElemType removeLast() {
        if (isEmpty()) {
            return null;
        }

        if (hollow()) {
            shrink();
        }
        size -= 1;
        nextlast = truePos(nextlast, -1);
        ElemType ret = array[nextlast];
        array[nextlast] = null;
        return ret;
    }

    @Override
    public ElemType get(int index) {
        if (index < 0)
            return null;
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
    public Iterator<ElemType> iterator() {
        return new Iterator<ElemType>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public ElemType next() {
                return get(index++);
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
