package info.kgeorgiy.ja.stafeev.exam.parallelquicksort;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelQuickSort {
    public static <T extends Comparable<? super T>> void sort(final int threads, final List<T> list) {
        sort(threads, list, Comparator.naturalOrder());
    }
    public static <T> void sort(final int threads, final List<T> list, Comparator<? super T> comparator) {
        new ForkJoinPool(threads).invoke(new ParallelQuickSort.Sorter<>(list, comparator, 0, list.size() - 1));
    }

    private static class Sorter<T> extends RecursiveAction {

        final List<T> list;
        final Comparator<? super T> comparator;
        final int left;
        final int right;

        public Sorter(final List<T> list, Comparator<? super T> comparator, int left, int right) {
            this.list = list;
            this.comparator = comparator;
            this.left = left;
            this.right = right;
        }

        /**
         * The main computation performed by this task.
         */

        @Override
        protected void compute() {

            if (right <= left) {
                return;
            }

            final T middle = list.get((left + right) / 2);
            int i = left;
            int j = right;

            while (i <= j) {
                while (comparator.compare(list.get(i), middle) < 0) {
                    ++i;
                }
                while (comparator.compare(middle, list.get(j)) < 0) {
                    --j;
                }
                if (i <= j) {
                    Collections.swap(list, i, j);
                    ++i;
                    --j;
                }
            }

            invokeAll(new Sorter<>(list, comparator, left, j), new Sorter<>(list, comparator, i, right));
        }
    }
}
