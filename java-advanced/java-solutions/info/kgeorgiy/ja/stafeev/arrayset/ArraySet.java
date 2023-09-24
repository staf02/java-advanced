package info.kgeorgiy.ja.stafeev.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {

    private static class InnerList<T> extends AbstractList<T> implements RandomAccess {
        private final List<T> data;
        private final boolean reversed;

        InnerList(InnerList<T> data) {
            this.data = data.data;
            this.reversed = !data.reversed;
        }

        InnerList(List<T> data, boolean reversed) {
            this.data = data;
            this.reversed = reversed;
        }

        @Override
        public T get(int ind) {
            return data.get(reversed ? size() - ind - 1 : ind);
        }

        @Override
        public int size() {
            return data.size();
        }
    }
    private final InnerList<T> data;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(final Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet(final Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(final Collection<? extends T> collection, final Comparator<? super T> comparator) {
        final Set<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        this.data = new InnerList<>(new ArrayList<>(treeSet), false);
        this.comparator = comparator;
    }

    private ArraySet(List<T> data, Comparator<? super T> cmp) {
        if (data instanceof InnerList<T> innerData) {
            this.data = new InnerList<>(innerData.data, innerData.reversed);
        } else {
            this.data = new InnerList<>(data, false);
        }
        this.comparator = cmp;
    }

    private T get(final int index) {
        return index >= 0 && index < data.size() ? data.get(index) : null;
    }

    /*If equals is true, then return equal element if it exists
     * If lower is true, then return lower element, else return bigger element*/
    private int getIndexByElement(final T element, final boolean equals, final boolean lower) {
        int index = Collections.binarySearch(data, element, comparator);
        if (index >= 0) {
            if (equals) {
                return index;
            }
            return index + (lower ? -1 : 1);
        }
        return (-index - 1) + (lower ? -1 : 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(final Object el) {
        return Collections.binarySearch(data, (T) el, comparator) >= 0;
    }

    @Override
    public T lower(final T t) {
        return get(getIndexByElement(t, false, true));
    }

    @Override
    public T floor(final T t) {
        return get(getIndexByElement(t, true, true));
    }

    @Override
    public T ceiling(final T t) {
        return get(getIndexByElement(t, true, false));
    }

    @Override
    public T higher(final T t) {
        return get(getIndexByElement(t, false, false));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new InnerList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private NavigableSet<T> subsetByIndexes(final int first, final int last) {
        if (first > last) {
            return new ArraySet<>();
        }
        return new ArraySet<>(data.subList(first, last + 1), comparator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public NavigableSet<T> subSet(final T fromElement, final boolean fromInclusive, T toElement, final boolean toInclusive) {
        if ((comparator != null ? comparator.compare(fromElement, toElement) : ((Comparable<T>) fromElement).compareTo(toElement)) > 0) {
            throw new IllegalArgumentException("Fucking shit");
        }
        final int fromIndex = getIndexByElement(fromElement, fromInclusive, false);
        final int toIndex = getIndexByElement(toElement, toInclusive, true);
        return subsetByIndexes(fromIndex, toIndex);
    }

    @Override
    public NavigableSet<T> headSet(final T toElement, final boolean inclusive) {
        return subsetByIndexes(0, getIndexByElement(toElement, inclusive, true));
    }

    @Override
    public NavigableSet<T> tailSet(final T fromElement, final boolean inclusive) {
        return subsetByIndexes(getIndexByElement(fromElement, inclusive, false), size() - 1);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(final T fromElement, final T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(final T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(final T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() throws NoSuchElementException {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    @Override
    public T last() throws NoSuchElementException {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(data.size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }
}
