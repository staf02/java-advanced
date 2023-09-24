package info.kgeorgiy.ja.stafeev.exam.parallelmergesort;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelMergeSort {

    public static <T extends Comparable<? super T>> void sort(final int threads, final List<T> list) {
        sort(threads, list, Comparator.naturalOrder());
    }
    public static <T> void sort(final int threads, final List<T> list, Comparator<? super T> comparator) {
        final List<T> mergeList = new ArrayList<>(list);
        new ForkJoinPool(threads).invoke(new Sorter<>(list, comparator, 0, list.size(), mergeList));
    }

    private static class Sorter<T> extends RecursiveAction {

        final List<T> list;
        final Comparator<? super T> comparator;
        final int left;
        final int right;

        final List<T> mergeList;
        public Sorter(final List<T> list, Comparator<? super T> comparator, int left, int right, final List<T> mergeList) {
            this.list = list;
            this.comparator = comparator;
            this.left = left;
            this.right = right;
            this.mergeList = mergeList;
        }

        /**
         * The main computation performed by this task.
         */

        @Override
        protected void compute() {
            if (right - left == 1) {
                return;
            }

            final int middle = (left + right) / 2;

            invokeAll(new Sorter<>(list, comparator, left, middle, mergeList),
                    new Sorter<>(list, comparator, middle, right, mergeList));

            int leftIndex = left, rightIndex = middle, index = leftIndex;

            while (leftIndex < middle && rightIndex < right) {
                if (comparator.compare(list.get(leftIndex), list.get(rightIndex)) > 0) {
                    mergeList.set(index++, list.get(rightIndex++));
                } else {
                    mergeList.set(index++, list.get(leftIndex++));
                }
            }
            while (leftIndex < middle) {
                mergeList.set(index++, list.get(leftIndex++));
            }
            while (rightIndex < right) {
                mergeList.set(index++, list.get(rightIndex++));
            }

            for (int i = leftIndex; i < rightIndex; i++) {
                list.set(i, mergeList.get(i));
            }
        }
    }
}
