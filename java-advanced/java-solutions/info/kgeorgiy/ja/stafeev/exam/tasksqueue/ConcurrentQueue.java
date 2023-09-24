package info.kgeorgiy.ja.stafeev.exam.tasksqueue;

import java.util.ArrayDeque;
import java.util.Queue;

public class ConcurrentQueue<T> {
    private final Queue<T> tasks;
    private static final int MAX_SIZE = 10;

    public ConcurrentQueue() {
        tasks = new ArrayDeque<>();
    }

    public void add(final T value) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() >= MAX_SIZE) {
                tasks.wait();
            }
            tasks.add(value);
            tasks.notifyAll();
        }
    }

    public T poll() throws InterruptedException {
        final T result;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            result = tasks.poll();
            tasks.notifyAll();
        }
        return result;
    }
}
