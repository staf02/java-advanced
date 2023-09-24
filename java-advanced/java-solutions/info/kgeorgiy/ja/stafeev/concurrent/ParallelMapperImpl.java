package info.kgeorgiy.ja.stafeev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ParallelMapperImpl implements ParallelMapper {

    private final Queue<Runnable> tasks;
    private final List<Thread> workers;


    public ParallelMapperImpl(final int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Expected positive count of threads, found " + threads);
        }

        this.tasks = new ArrayDeque<>();
        this.workers = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(this::threadFunction));
        }
        workers.forEach(Thread::start);
    }

    private void threadFunction() {
        try {
            while (!Thread.interrupted()) {
                final Runnable task;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        tasks.wait();
                    }
                    task = tasks.poll();
                }
                task.run();
            }
        } catch (final InterruptedException ignored) {

        } finally {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        TaskExecutor<T, R> taskExecutor = new TaskExecutor<>(f, args);
        return taskExecutor.getResult();
    }

    private void stopThread(final Thread thread) {
        thread.interrupt();
        try {
            thread.join();
        } catch (final InterruptedException ignored) {

        }
    }

    @Override
    public void close() {
        workers.forEach(this::stopThread);
    }

    private class TaskExecutor<T, R> {
        private int argsCount;
        private final List<R> result;
        private final Function<? super T, ? extends R> f;
        private final List<? extends T> args;
        private RuntimeException exception;

        public TaskExecutor(Function<? super T, ? extends R> f, List<? extends T> args) {
            this.argsCount = args.size();
            this.result = new ArrayList<>(Collections.nCopies(argsCount, null));

            this.f = f;
            this.args = args;

            IntStream.range(0, args.size()).forEach(this::addTask);
        }

        private void addTask(final int i) {
            synchronized (tasks) {
                tasks.add(() -> {
                    R fResult = null;
                    try {
                        fResult = f.apply(args.get(i));
                    } catch (final RuntimeException runtimeException) {
                        synchronized (this) {
                            if (exception == null) {
                                exception = runtimeException;
                            } else {
                                exception.addSuppressed(runtimeException);
                            }
                        }
                    }
                    synchronized (this) {
                        result.set(i, fResult);
                        argsCount--;
                        if (argsCount == 0) {
                            notify();
                        }
                    }
                });
                tasks.notify();
            }
        }

        public synchronized List<R> getResult() throws InterruptedException {
            while (argsCount != 0) {
                wait();
            }
            if (exception != null) {
                throw exception;
            }
            return result;
        }
    }
}