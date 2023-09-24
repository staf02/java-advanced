package info.kgeorgiy.ja.stafeev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WebCrawler implements AdvancedCrawler {

    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService downloadExecutor;
    private final ExecutorService extractExecutor;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;

        this.downloadExecutor = Executors.newFixedThreadPool(downloaders);
        this.extractExecutor = Executors.newFixedThreadPool(extractors);
    }

    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @param hosts domains to follow, pages on another domains should be ignored.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth, List<String> hosts) {
        final Set<String> permittedHosts = hosts.stream().collect(Collectors.toUnmodifiableSet());
        return new LinearDownloader(url, depth, permittedHosts::contains).linearDownload();
    }

    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth) {
        return new LinearDownloader(url, depth, x -> true).linearDownload();
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        shutdownAndAwaitTermination(downloadExecutor);
        shutdownAndAwaitTermination(extractExecutor);
    }

    private static void shutdownAndAwaitTermination(final ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(final String[] args) throws IOException {
        if (args == null) {
            throw new IllegalArgumentException("args is null");
        }
        if (args.length == 0) {
            throw new IllegalArgumentException("args is empty");
        }
        final String url = args[0];
        final Downloader cachingDownloader = new CachingDownloader(1.0);
        final int[] array = new int[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            try {
                array[i - 1] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                System.err.println("arg number " + (i + 1) + " is not integer");
            }
        }
        final int depth = array.length > 0 ? array[0] : 2;
        final int downloaders = array.length > 1 ? array[1] : 2;
        final int extractors = array.length > 2 ? array[2] : 2;
        final int perhost = array.length > 3 ? array[3] : 2;
        try (final Crawler crawler = new WebCrawler(cachingDownloader, downloaders, extractors, perhost)) {
            Result result = crawler.download(url, depth);
            System.out.println("Loaded sites:");
            for (final String site : result.getDownloaded()) {
                System.out.println(site);
            }
        }
    }

    private class LinearDownloader {
        private final int depth;
        private final Phaser phaser;
        private final Set<String> urls;
        private final Set<String> processed;
        private final Set<String> nextLevel;

        private final Predicate<String> predicate;
        private final Map<String, IOException> errors;

        private final Map<String, TaskRecord> tasksQueues;

        public LinearDownloader(final String url, final int depth, final Predicate<String> predicate) {
            this.depth = depth;
            this.predicate = predicate;

            phaser = new Phaser(1);
            urls = ConcurrentHashMap.newKeySet();
            processed = ConcurrentHashMap.newKeySet();
            nextLevel = ConcurrentHashMap.newKeySet();
            errors = new ConcurrentHashMap<>();
            tasksQueues = new ConcurrentHashMap<>();

            urls.add(url);
        }

        public Result linearDownload() {
            for (int i = 0; i < depth; i++) {
                for (final String url : urls) {
                    if (!processed.contains(url) && !errors.containsKey(url)) {
                        try {
                            final String host = URLUtils.getHost(url);
                            if (predicate.test(host)) {
                                tasksQueues.putIfAbsent(host, new TaskRecord(perHost));
                                submitHost(host, url);
                                processed.add(url);
                            }
                        } catch (final MalformedURLException exception) {
                            errors.put(url, exception);
                        }
                    }
                }

                phaser.arriveAndAwaitAdvance();

                urls.addAll(nextLevel);
                nextLevel.clear();
            }
            return new Result(processed.stream().filter(url -> !errors.containsKey(url)).toList(), errors);
        }

        private void submitHost(final String host, final String url) {
            synchronized (tasksQueues.get(host)) {
                final Runnable resultRunnable = getRunnable(() -> download(url), host);
                final TaskRecord taskRecord = tasksQueues.get(host);
                if (taskRecord.limit > 0) {
                    taskRecord.limit--;
                    phaser.register();
                    downloadExecutor.submit(resultRunnable);
                }
                else {
                    taskRecord.tasks.add(resultRunnable);
                }
            }
        }

        private Runnable getRunnable(final Runnable runnable, final String host) {
            return () -> {
                runnable.run();
                Runnable poll;
                synchronized (tasksQueues.get(host)) {
                    final TaskRecord taskRecord = tasksQueues.get(host);
                    poll = taskRecord.tasks.poll();
                    if (poll == null) {
                        taskRecord.limit++;
                    }
                }
                if (poll != null) {
                    phaser.register();
                    downloadExecutor.submit(poll);
                }
            };
        }

        private void download(final String url) {
            try {
                final Document document = downloader.download(url);
                extractLinks(url, document);
            } catch (final IOException exception) {
                errors.put(url, exception);
            }
            phaser.arriveAndDeregister();
        }

        private void extractLinks(final String url, final Document document) {
            phaser.register();
            extractExecutor.submit(() -> {
                try {
                    nextLevel.addAll(document.extractLinks());
                } catch (final IOException exception) {
                    errors.put(url, exception);
                } finally {
                    phaser.arriveAndDeregister();
                }

            });
        }

        private static class TaskRecord {

            public final Queue<Runnable> tasks;
            public int limit;

            public TaskRecord(int limit) {
                this.tasks = new ArrayDeque<>();
                this.limit = limit;
            }
        }
    }
}
