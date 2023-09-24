package info.kgeorgiy.ja.stafeev.exam.httpexecutor;

import info.kgeorgiy.ja.stafeev.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class HttpExecutor implements AutoCloseable {

    private final ScheduledExecutorService executorService;
    private final List<HttpTask> tasks;
    private final BlockingQueue<String> results;
    private CountDownLatch latch;

    private final DateFormat format;
    private final Logger logger;

    public HttpExecutor(final DateFormat format, final Logger logger) {
        this.tasks = new ArrayList<>();
        this.results = new LinkedBlockingDeque<>();
        this.executorService = new ScheduledThreadPoolExecutor(8);


        this.format = format;
        this.logger = logger;
    }

    private void printResults() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                final String result = results.take();
                System.out.println(result);
                latch.countDown();
            }
        } catch (final InterruptedException ignored) {
        } finally {
            Thread.currentThread().interrupt();
        }

    }

    public void start() {
        if (tasks.isEmpty()) {
            return;
        }
        latch = new CountDownLatch(tasks.size());
        executorService.submit(this::printResults);
        for (final HttpTask task : tasks) {
            executorService.schedule(task,
                    Math.max(0, task.getExecutionTime() - System.currentTimeMillis()),
                    TimeUnit.MILLISECONDS);
        }

    }
    public void waitUntilFinish() throws InterruptedException {
        latch.await();
    }

    @Override
    public void close() {
        logger.logLocalizedMessage("stopping-executor");
        shutdownAndAwaitTermination(executorService);
        logger.logLocalizedMessage("stopped-executor");
    }

    private void shutdownAndAwaitTermination(final ExecutorService pool) {
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.logLocalizedError("sys-error");
                }
            }
        } catch (final InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void parseTasks(final Path path) throws IOException {
        final Consumer<String> onCallBack = results::add;
        final BiFunction<Date, String, String> makeResultMessage =
                (date, s) -> logger.makeMessage("success", date, s);
        final BiFunction<Date, Exception, String> makeExceptionMessage =
                (date, exception) -> logger.makeMessage("error", date, exception);
        try (final BufferedReader bufferedReader = Files.newBufferedReader(path)){
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    final ParsePosition pos = new ParsePosition(0);
                    final Date date = format.parse(line, pos);
                    if (date == null) {
                        logger.logLocalizedError("bad-date", line);
                        continue;
                    }
                    final String[] ans = line.substring(pos.getIndex() + 1).split(" ");

                    tasks.add(new HttpTask(date, new URL(ans[0]), ans[1], onCallBack, makeResultMessage, makeExceptionMessage));
                }
            } catch (final MalformedURLException e) {
                throw new IOException(logger.makeMessage("bad-url", e));
            } catch (final IOException e) {
                throw new IOException(logger.makeMessage("cannot-read-from-file", e));
            }
        } catch (final IOException e) {
            throw new IOException(logger.makeMessage("cannot-open-file", e));
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Usage: HttpExecutor <input file> <locale>");
            return;
        }

        final Locale locale = Locale.forLanguageTag(args[1]);
        final ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.stafeev.exam.httpexecutor.HttpBundle", locale);
        } catch (final MissingResourceException e) {
            System.err.println("Supported only en_US and ru_RU");
            return;
        }

        final Logger logger = new Logger(bundle);
        final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", locale);
        try (final HttpExecutor httpExecutor = new HttpExecutor(dateFormat, logger)) {

            httpExecutor.parseTasks(Path.of(args[0]));
            httpExecutor.start();
            try {
                httpExecutor.waitUntilFinish();
            } catch (final InterruptedException e) {
                logger.logLocalizedError("interrupted", e);
            }
        } catch (final InvalidPathException e) {
            logger.logLocalizedError("invalid-path", e);
        } catch (final IOException e) {
            logger.logLocalizedException(e);
        }
    }
}
