package info.kgeorgiy.ja.stafeev.exam.memorizer;

import info.kgeorgiy.ja.stafeev.exam.tasksqueue.ConcurrentQueue;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

public class Memorizer<T, R> implements AutoCloseable {
    private final Function<T, R> function;
    private final Map<T, R> cache;
    private final ExecutorService executorService;

    Memorizer(Function<T, R> function, final int threads) {
        this.function = function;

        cache = new ConcurrentHashMap<>();
        executorService = Executors.newFixedThreadPool(threads);
    }

    public R apply(final T arg) throws MemorizerException {
        Future<R> functionResult = executorService.submit(() -> cache.computeIfAbsent(arg, function));
        try {
            return functionResult.get();
        } catch (final ExecutionException | InterruptedException exception) {
            throw new MemorizerException("Cannot execute function : memorizer was interrupted");
        }
    }

    @Override
    public void close() {
        shutdownAndAwaitTermination();
    }

    private void shutdownAndAwaitTermination() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
