package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 *
 * Assumes null keys will never be inserted, and does not resize down upon
 * remove().
 * 
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size = 0;
    private int MAXSIZE = 16;
    private double loadFactor = 0.75;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(MAXSIZE);
    }

    public MyHashMap(int initialSize) {
        MAXSIZE = initialSize;
        buckets = createTable(MAXSIZE);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        MAXSIZE = initialSize;
        this.loadFactor = maxLoad;
        buckets = createTable(MAXSIZE);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            private int index = 0;
            private int left = size;
            Iterator<Node> bucket = buckets[0].iterator();

            @Override
            public boolean hasNext() {
                return left != 0;
            }

            @Override
            public K next() {
                if (bucket.hasNext()) {
                    left -= 1;
                    return bucket.next().key;
                } else {
                    if (index < MAXSIZE - 1) {
                        bucket = buckets[++index].iterator();
                        return next();
                    } else {
                        return null;
                    }
                }
            }
        };
    }

    @Override
    public void clear() {
        size = 0;
        buckets = createTable(MAXSIZE);
    }

    @Override
    public boolean containsKey(K key) {
        int index = truePose(key);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int index = truePose(key);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    private Node getNode(K key) {
        int index = truePose(key);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private int truePose(K key) {
        return Math.floorMod(key.hashCode(), MAXSIZE);
    }

    private boolean isFull() {
        return (double) size / MAXSIZE > loadFactor;
    }

    private void resize() {
        Collection<Node>[] old_buckets = buckets;
        clear();
        for (Collection<Node> bucket : old_buckets) {
            for (Node node : bucket) {
                put(node.key, node.value);
            }
        }
    }

    @Override
    public void put(K key, V value) {
        Node node = getNode(key);
        if (node != null) {
            node.value = value;
        } else {
            buckets[truePose(key)].add(createNode(key, value));
            size += 1;
        }

        if (isFull()) {
            MAXSIZE *= 2;
            resize();
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> s = new HashSet();

        for (K key : this) {
            s.add(key);
        }
        return s;
    }

    @Override
    public V remove(K key) {
        Node node = getNode(key);
        if (node == null) {
            return null;
        } else {
            size -= 1;
            buckets[truePose(key)].remove(node);
            return node.value;
        }
    }

    @Override
    public V remove(K key, V value) {
        Node node = getNode(key);
        if (node == null) {
            return null;
        } else {
            if (node.value.equals(value)) {
                size -= 1;
                buckets[truePose(key)].remove(node);
                return node.value;
            } else {
                return null;
            }
        }
    }
}
