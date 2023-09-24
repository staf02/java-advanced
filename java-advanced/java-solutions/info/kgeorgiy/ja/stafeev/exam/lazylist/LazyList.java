package info.kgeorgiy.ja.stafeev.exam.lazylist;

import java.util.*;

public class LazyList<T> extends AbstractList<T> implements List<T> {

    private class MutableOptional {
        T value;
        boolean isCalculated;

        MutableOptional() {
            value = null;
            isCalculated = false;
        }
    }

    private final int size;
    private final ElementCalculator<T> elementCalculator;
    private final List<MutableOptional> data;

    public LazyList(final int size, final ElementCalculator<T> elementCalculator) {
        this.size = size;
        this.elementCalculator = elementCalculator;

        this.data = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            data.add(new MutableOptional());
        }
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }

        synchronized (data.get(index)) {
            if (!data.get(index).isCalculated) {
                data.get(index).value = elementCalculator.calculate(index);
                data.get(index).isCalculated = true;
            }
        }

        return data.get(index).value;
    }

    @Override
    public int size() {
        return size;
    }
}
