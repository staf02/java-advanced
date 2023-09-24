package info.kgeorgiy.ja.stafeev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {

    private final ParallelMapper parallelMapper;
    private Exception exception;

    public IterativeParallelism() {
        parallelMapper = null;
    }

    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T, R> R parallelCalc(int threads,
                                  final List<T> list,
                                  final Function<Stream<T>, R> mapper,
                                  final Function<Stream<R>, R> reducer) throws InterruptedException {
        exception = null;

        if (threads <= 0) {
            throw new IllegalArgumentException("Expected positive count of threads, found " + threads);
        }

        threads = Math.min(threads, list.size());
        List<Stream<T>> tasks = splitToTasks(threads, list);

        List<R> result;
        if (parallelMapper != null) {
            result = parallelMapper.map(mapper, tasks);
        } else {
            result = new ArrayList<>(Collections.nCopies(threads, null));
            List<Thread> workers = new ArrayList<>();

            IntStream.range(0, threads).forEach(i -> workers.add(new Thread(() -> {
                try {
                    R mapperResult = mapper.apply(tasks.get(i));
                    result.set(i, mapperResult);
                } catch (final RuntimeException e) {
                    synchronized (this) {
                        if (exception == null) {
                            exception = e;
                        } else {
                            exception.addSuppressed(e);
                        }
                    }
                }
            }
            ))
            );
            workers.forEach(Thread::start);

            joinThreads(workers);
        }
        if (exception != null) {
            System.err.println("Exception was thrown while calculating : " + exception.getMessage());
        }
        return reducer.apply(result.stream());
    }

    private <T> List<Stream<T>> splitToTasks(final int threads, final List<T> list) {
        List<Stream<T>> subLists = new ArrayList<>();
        int block = list.size() / threads;
        int blockStart = 0, blockEnd;

        for (int i = 0; i < threads; i++) {
            blockEnd = blockStart + (i < list.size() % threads ? block + 1 : block);
            subLists.add(list.subList(blockStart, blockEnd).stream());
            blockStart = blockEnd;
        }
        return subLists;
    }

    private void joinThreads(final List<Thread> workers) {
        for (int i = 0; i < workers.size(); i++) {
            try {
                workers.get(i).join();
            } catch (final InterruptedException e) {
                for (int j = i; j < workers.size(); j++) {
                    if (!workers.get(j).isInterrupted()) {
                        workers.get(j).interrupt();
                    }
                }
                i--;
            }
        }
    }

    @Override
    public <T> T reduce(final int i, final List<T> list, final Monoid<T> monoid) throws InterruptedException {
        return parallelCalc(i, list, stream -> stream.reduce(monoid.getIdentity(), monoid.getOperator()),
                stream -> stream.reduce(monoid.getIdentity(), monoid.getOperator()));
    }

    @Override
    public <T, R> R mapReduce(final int i, final List<T> list, final Function<T, R> function, final Monoid<R> monoid) throws InterruptedException {
        return parallelCalc(i, list, stream -> stream.map(function).reduce(monoid.getIdentity(), monoid.getOperator()),
                stream -> stream.reduce(monoid.getIdentity(), monoid.getOperator()));
    }

    @Override
    public String join(final int i, final List<?> list) throws InterruptedException {
        return parallelCalc(i, list, stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(final int i, final List<? extends T> list, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelCalc(i, list, stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(final int i, final List<? extends T> list, final Function<? super T, ? extends U> function) throws InterruptedException {
        return parallelCalc(i, list, stream -> stream.map(function).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(final int i, final List<? extends T> list, final Comparator<? super T> comparator) throws InterruptedException {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("values is empty");
        }
        return parallelCalc(i, list, stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    @Override
    public <T> T minimum(final int i, final List<? extends T> list, final Comparator<? super T> comparator) throws InterruptedException {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("values is empty");
        }
        return parallelCalc(i, list, stream -> stream.min(comparator).orElseThrow(),
                stream -> stream.min(comparator).orElseThrow());
    }

    @Override
    public <T> boolean all(final int i, final List<? extends T> list, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelCalc(i, list, stream -> stream.allMatch(predicate),
                booleanStream -> booleanStream.reduce(true, Boolean::logicalAnd));
    }

    @Override
    public <T> boolean any(final int i, final List<? extends T> list, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelCalc(i, list, stream -> stream.anyMatch(predicate),
                booleanStream -> booleanStream.reduce(false, Boolean::logicalOr));
    }

    @Override
    public <T> int count(final int i, final List<? extends T> list, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelCalc(i, list, l -> l.map(x -> predicate.test(x) ? 1 : 0).reduce(0, Integer::sum),
                integerStream -> integerStream.reduce(0, Integer::sum));
    }
}
