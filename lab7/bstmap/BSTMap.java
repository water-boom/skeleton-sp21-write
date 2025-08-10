package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left, right;
        public int size;

        BSTNode (K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }

    private BSTNode root;


    public BSTMap() {
        root = null;
    }
    @Override
    public void clear(){
        root = null;
    }

    public boolean containsKey(K key){
        if (key == null) throw new IllegalArgumentException("argument to contains() is null");
        return containsKey(root, key);
    }
    private boolean containsKey(BSTNode x, K key) {
        if (x == null) return false;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) return containsKey(x.left, key);
        else if (cmp > 0) return containsKey(x.right, key);
        else return true;
    }

    public V get(K key){
        return get(root, key);
    }

    private V get(BSTNode x, K key) {
        if (key == null) throw new IllegalArgumentException("calls get() with a null key");
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) return get(x.left, key);
        else if (cmp > 0) return get(x.right, key);
        else              return x.value;
    }

    public int size() {
        return size(root);
    }

    private int size(BSTNode x) {
        if (x == null) return 0;
        else return x.size;
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("calls put() with a null key");
        root = put(root,key,value);
    }
    private BSTNode put(BSTNode x, K key, V value) {
        if (x == null) return new BSTNode(key, value,1);
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) x.left  = put(x.left,  key, value);
        else if (cmp > 0) x.right = put(x.right, key, value);
        else              x.value   = value;
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("keySet not implemented");
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("remove not implemented");
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("remove with value not implemented");
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("iterator not implemented");
    }

    public void printInOrder() {
        printInOrder(root);
        System.out.println();

    }
    private void printInOrder(BSTNode x) {
        if (x == null) return;
        printInOrder(x.left);
        System.out.println(x.key);
        printInOrder(x.right);
    }
}
