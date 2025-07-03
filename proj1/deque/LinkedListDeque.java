package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T>{
    //Node
    private class Node {
        T item;
        Node next;
        Node prev;

        public Node(T item) {
            this.item = item;
            this.next = null;
            this.prev = null;
        }
    }
    public int size;
    public Node sentinel;
    public LinkedListDeque(){

        sentinel = new Node(null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }
@Override
    public void addFirst(T item){
        size+=1;
        Node newNode = new Node(item);
        if(sentinel.next == sentinel) {
            sentinel.next = newNode;
            sentinel.prev = newNode;
            newNode.next = sentinel;
            newNode.prev = sentinel;
        }else{
            newNode.next = sentinel.next;
            newNode.next.prev =newNode;
            newNode.prev = sentinel;
            sentinel.next = newNode;
        }
    }
@Override
    public void addLast(T item){
        size+=1;
        Node newNode = new Node(item);
        if(sentinel.next == sentinel) {
            sentinel.next = newNode;
            sentinel.prev = newNode;
            newNode.next = sentinel;
            newNode.prev = sentinel;
        }else{
            newNode.prev = sentinel.prev;
            newNode.prev.next = newNode;
            newNode.next = sentinel;
            sentinel.prev = newNode;
        }
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public int size(){
        return size;
    }

    public T removeFirst(){
        T item = sentinel.next.item;

        if (sentinel.next == sentinel) {
            return null;
        }
        Node removeNode = sentinel.next;
        sentinel.next = removeNode.next;
        removeNode.next.prev = sentinel;
        removeNode = null;
        size-=1;
        return item;
    }

    public T removeLast(){
        T item = sentinel.prev.item;
        if (sentinel.next == sentinel) {
            return null;
        }
        Node removeNode = sentinel.prev;
        sentinel.prev = removeNode.prev;
        removeNode.prev.next = sentinel;
        removeNode =null;
        size-=1;
        return item;
    }

    public T get(int index){
        //use iteration
        if(index < 0 || index >= size) return null;
        Node current = sentinel.next;
        for(int i = 0; i < index; i++){
            current = current.next;
        }
        return current.item;
    }

    public T getRecursive(int index){
        //use recursion
        if(index < 0 || index >= size) return null;
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(Node current, int index) {
        if (index == 0) {
            return current.item;
        }
        return getRecursiveHelper(current.next, index - 1);
    }
@Override
    public void printDeque() {
        Node p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }
@Override
public Iterator<T> iterator() {
    return new LinkedListDequeIterator();
}
    private class LinkedListDequeIterator implements Iterator<T> {
        private Node current = sentinel.next;

        @Override
        public boolean hasNext() {
            return current != sentinel;
        }

        @Override
        public T next() {
            T item = current.item;
            current = current.next;
            return item;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Deque<?>)) return false;

        Deque<?> other = (Deque<?>) o;
        if (other.size() != this.size()) return false;

        for (int i = 0; i < size(); i++) {
            if (!get(i).equals(other.get(i))) return false;
        }
        return true;
    }
}
