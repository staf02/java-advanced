package info.kgeorgiy.ja.stafeev.exam.bag;

import java.util.*;

public class Bag<T> implements Collection<T> {

    private final Map<T, List<T>> data;
    private int size;

    Bag() {
        data = new HashMap<>();
        size = 0;
    }
    Bag(final Collection<? extends T> collection) {
        data = new HashMap<>();
        size = 0;
        addAll(collection);
    }

    private class BagIterator implements Iterator<T> {

        private final Iterator<List<T>> mapIterator;
        private Iterator<T> listIterator;


        BagIterator() {
            mapIterator = data.values().iterator();
            listIterator = mapIterator.next().iterator();
            hasNext();
        }

        @Override
        public boolean hasNext() {
            if (listIterator.hasNext()) {
                return true;
            } else {
                while (mapIterator.hasNext()) {
                    final Iterator<T> iterator = mapIterator.next().iterator();
                    if (iterator.hasNext()) {
                        listIterator = iterator;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public T next() {
            if (hasNext()) {
                return listIterator.next();
            }
            return null;
        }

        @Override
        public void remove() {
            listIterator.remove();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(final Object o) {
        return data.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new BagIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[size];
        int index = 0;
        for (T elem : this) {
            objects[index++] = elem;
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T1> T1[] toArray(T1[] a) {
        Object[] objects = new Object[size];
        int index = 0;
        for (T elem : this) {
            objects[index++] = elem;
        }
        return (T1[]) objects;
    }

    @Override
    public boolean add(final T t) {
        data.putIfAbsent(t, new ArrayList<>());
        size++;
        return data.get(t).add(t);
    }

    @Override
    public boolean remove(final Object o) {
        boolean result = contains(o) && data.get(o).remove(o);
        if (result) {
            size--;
        }
        return result;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        boolean result = true;
        for (final Object o : c) {
            result &= contains(o);
        }
        return result;
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        boolean result = true;
        for (final T t : c) {
            result &= add(t);
        }
        return result;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean result = true;
        for (final Object t : c) {
            result &= remove(t);
        }
        return result;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return removeIf(t -> !c.contains(t));
    }

    @Override
    public void clear() {
        data.clear();
        size = 0;
    }
}
