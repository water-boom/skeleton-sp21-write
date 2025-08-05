package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> comparator;

    public T max() {
        if (isEmpty()) return null;
        // If no comparator is provided, use natural ordering
        T maxItem = get(0);
        for (int i = 1; i < size(); i++) {
            T current = get(i);
            if (comparator.compare(current, maxItem) > 0) {
                maxItem = current;
            }
        }
        return maxItem;
    }
    public T max(Comparator<T> c){
        // If a comparator is provided, use it to find the maximum element
        if(isEmpty()) return null;
        T maxItem = items[0];
        for (int i = 1; i < size; i++) {
            if (c.compare(items[i], maxItem) > 0) {
                maxItem = items[i];
            }
        }
        return maxItem;
    }
    public MaxArrayDeque(Comparator<T> c){
        super();
        comparator = c;
    }
}


