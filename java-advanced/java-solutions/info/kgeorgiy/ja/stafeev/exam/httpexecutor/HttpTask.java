package info.kgeorgiy.ja.stafeev.exam.httpexecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class HttpTask implements Runnable, Comparable<HttpTask> {
    private final long executionTime;
    private final Date date;
    private final URL url;
    private final String query;
    private final Consumer<String> onCallBack;
    private final BiFunction<Date, String, String> makeResultMessage;
    private final BiFunction<Date, Exception, String> makeExceptionMessage;

    public HttpTask(final Date date,
                    final URL url,
                    final String query,
                    final Consumer<String> onCallBack,
                    final BiFunction<Date, String, String> makeResultMessage,
                    final BiFunction<Date, Exception, String> makeExceptionMessage) {

        this.date = date;
        this.executionTime = date.getTime();
        this.url = url;
        this.query = query;
        this.onCallBack = onCallBack;
        this.makeResultMessage = makeResultMessage;
        this.makeExceptionMessage = makeExceptionMessage;
    }

    @Override
    public int compareTo(final HttpTask o) {
        return Long.compare(executionTime, o.executionTime);
    }

    @Override
    public void run() {
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // :NOTE: * query is not request method
            connection.setRequestMethod(query);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8));
            String inputLine;
            final StringBuilder content = new StringBuilder();
            while (!Thread.currentThread().isInterrupted() && (inputLine = bufferedReader.readLine()) != null) {
                content.append(inputLine);
            }
            onCallBack.accept(makeResultMessage.apply(date, content.toString()));
        } catch (final IOException e) {
            onCallBack.accept(makeExceptionMessage.apply(date, e));
        }
    }

    public long getExecutionTime() {
        return executionTime;
    }
}
