package info.kgeorgiy.ja.stafeev.exam.lazylist;

import org.junit.BeforeClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class LazyListTest {

    private static class IdenticalCalculator implements ElementCalculator<Integer> {

        AtomicInteger integer = new AtomicInteger(0);
        @Override
        public Integer calculate(int index) {
            return index;
        }
    }

    @Test
    public void simpleTest() throws InterruptedException {
        LazyList<Integer> list = new LazyList<>(5, new IdenticalCalculator());

        Thread first = new Thread(() -> {
            assertEquals(3, (long) list.get(3));
        });
        Thread second = new Thread(() -> {
            assertEquals(3, (long) list.get(3));
        });

        first.start();
        second.start();
        first.join();
        second.join();
    }

    private void doCycle(LazyList<Integer> list) {
        for (int i = 0; i < 100; ++i) {
            assertEquals(i, (long) list.get(i));
        }
    }

    private void doReversedCycle(LazyList<Integer> list) {
        for (int i = 99; i >= 0; --i) {
            assertEquals(i, (long) list.get(i));
        }
    }

    @Test
    public void cycleTest() throws InterruptedException {
        LazyList<Integer> list = new LazyList<>(100, new IdenticalCalculator());

        Thread first = new Thread(() -> {
            doCycle(list);
        });
        Thread second = new Thread(() -> {
            doReversedCycle(list);
        });

        first.start();
        second.start();
        first.join();
        second.join();
    }
}