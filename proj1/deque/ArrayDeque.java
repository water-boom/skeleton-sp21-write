package deque;

public class ArrayDeque<T> {
    T[] items;
    int size;

    public ArrayDeque(){
        items = (T[]) new Object[8];
        size = 0;
    }
    public void resize(int capacity){
        T[] newItems = (T[]) new Object[capacity];
        if (size >= 0) System.arraycopy(items, 0, newItems, 0, size);
        items = newItems;

    }

    public void addFirst(T item){
        if (size == items.length) {
            resize(items.length * 2);
        }
        for (int i = size; i > 0; i--) {
            items[i] = items[i - 1];
        }
        items[0] = item;
        size++;
    }

    public void addLast(T item){
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[size] = item;
        size++;
    }

    public T removeFirst(){
        if (isEmpty()) {
            return null;
        }
        T item = items[0];
        for (int i = 0; i < size - 1; i++) {
            items[i] = items[i + 1];
        }
        items[size - 1] = null; // Clear the last item
        size--;
        if (size > 0 && size == items.length / 4) {
            resize(items.length / 2);
        }
        return item;
    }

    public T removeLast(){
        if (isEmpty()) {
            return null;
        }
        T item = items[size - 1];
        items[size - 1] = null; // Clear the last item
        size--;
        if (size > 0 && size == items.length / 4) {
            resize(items.length / 2);
        }
        return item;
    }

    public T get(int index){
        if (index < 0 || index >= size) {
            return null;
        }
        return items[index];
    }

    public int size(){
        return size;
    }

    public boolean isEmpty(){
        return size ==0;
    }
}
