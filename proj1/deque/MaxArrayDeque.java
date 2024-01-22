package deque;

import java.util.Comparator;

public class MaxArrayDeque<ElemType> extends ArrayDeque<ElemType> {
    private Comparator<ElemType> comparator;

    public MaxArrayDeque(Comparator<ElemType> c) {
        comparator = c;
    }

    public ElemType max() {
        if (isEmpty())
            return null;
        ElemType max = get(0);
        for (int i = 1; i < size(); i++) {
            if (comparator.compare(get(i), max) > 0) {
                max = get(i);
            }
        }
        return max;
    }

    public ElemType max(Comparator<ElemType> c) {
        if (isEmpty())
            return null;
        ElemType max = get(0);
        for (int i = 1; i < size(); i++) {
            if (c.compare(get(i), max) > 0) {
                max = get(i);
            }
        }
        return max;
    }

    public static void main(String[] args) {
        Comparator<Integer> c = new Comparator<>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            };
        };

        MaxArrayDeque<Integer> l = new MaxArrayDeque<>(c);
        for (int i = 0; i < 32; i++) {
            l.addFirst(i);
        }
        System.out.println(l.max());

        c = new Comparator<>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return -o1.compareTo(o2);
            };
        };
        System.out.println(l.max(c));
    }
}
