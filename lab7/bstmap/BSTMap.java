package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        public BSTNode left, right;
        public K key;
        public V value;

        BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private BSTNode root;
    private int size;

    public BSTMap() {
        size = 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(BSTNode p, K key) {
        if (p == null) {
            return false;
        } else if (key.compareTo(p.key) > 0) {
            return containsKey(p.right, key);
        } else if (key.compareTo(p.key) < 0) {
            return containsKey(p.left, key);
        } else {
            return true;
        }
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode p, K key) {
        if (p == null) {
            return null;
        } else if (key.compareTo(p.key) > 0) {
            return get(p.right, key);
        } else if (key.compareTo(p.key) < 0) {
            return get(p.left, key);
        } else {
            return p.value;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
        size += 1;

        // Iterative
        // size += 1;
        // if (root == null) {
        //     root = new BSTNode(key, value);
        //     return;
        // }
        // BSTNode p = root;
        // while (p != null) {
        //     if (key.compareTo(p.key) > 0) {
        //         if (p.right == null) {
        //             p.right = new BSTNode(key, value);
        //             return;
        //         } else {
        //             p = p.right;
        //         }
        //     } else if (key.compareTo(p.key) < 0) {
        //         if (p.left == null) {
        //             p.left = new BSTNode(key, value);
        //             return;
        //         } else {
        //             p = p.left;
        //         }
        //     } else {
        //         p.value = value;
        //         return;
        //     }
        // }
    }

    private BSTNode put(BSTNode p, K key, V value) {
        if (p == null) {
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(p.key);
        if (cmp > 0) {
            p.right = put(p.right, key, value);
        } else if (cmp < 0) {
            p.left = put(p.left, key, value);
        } else {
            p.value = value;
        }
        return p;
    }

    @Override
    public Set<K> keySet() {
        Set<K> s = new HashSet<>();
        for (K k : this) {
            s.add(k);
        }
        return s;
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIterator();
    }

    private class BSTMapIterator implements Iterator<K> {

        private Stack<BSTNode> stack;

        public BSTMapIterator() {
            stack = new Stack<>();
            allWayToLeft(root);
        }

        void allWayToLeft(BSTNode p) {
            while (p != null) {
                stack.add(p);
                p = p.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            BSTNode p = stack.pop();
            allWayToLeft(p.right);
            return p.key;
        }
    };

    @Override
    public V remove(K key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public V remove(K key, V value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    private void printInOrder(BSTNode p) {
        if (p == null) {
            return;
        }
        printInOrder(p.left);
        System.out.print(p.key);
        printInOrder(p.right);
    }

    public void printInOrder() {
        if (root == null) {
            throw new NullPointerException("root is null");
        }
        printInOrder(root);
    }
}
