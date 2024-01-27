package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        public BSTNode left, right;
        public K key;
        public V value;

        BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }
    }

    private BSTNode root;
    private int size;

    public BSTMap() {
        size = 0;
    }

    @Override
    public void clear() {
        clear(root);
        root = null;
        size = 0;
    }

    private void clear(BSTNode p) {
        if (p == null) {
            return;
        }
        clear(p.right);
        p.right = null;
        clear(p.left);
        p.left = null;
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
        size += 1;
        if (root == null) {
            root = new BSTNode(key, value);
            return;
        }
        BSTNode p = root;
        while (p != null) {
            if (key.compareTo(p.key) > 0) {
                if (p.right == null) {
                    p.right = new BSTNode(key, value);
                    return;
                } else {
                    p = p.right;
                }
            } else if (key.compareTo(p.key) < 0) {
                if (p.left == null) {
                    p.left = new BSTNode(key, value);
                    return;
                } else {
                    p = p.left;
                }
            } else {
                p.value = value;
                return;
            }
        }
    }

    @Override
    public Set<K> keySet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keySet'");
    }

    @Override
    public Iterator<K> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

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
