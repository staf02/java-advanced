package info.kgeorgiy.ja.stafeev.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Logger {
    private final ResourceBundle bundle;

    public Logger(final ResourceBundle resourceBundle) {
        bundle = resourceBundle;
    }

    public String loadLocalized(final String key) {
        return bundle.getString(key);
    }

    public String makeMessage(final String key, final Object... objects) {
        String format = "{0}";
        if (objects.length != 0) {
            format = String.join(" ", new String[]{format, ":",
                    IntStream.range(1, objects.length + 1).mapToObj(i -> "{" + i + "}").collect(Collectors.joining(" "))});
        }
        final List<String> args = new ArrayList<>(List.of(loadLocalized(key)));
        Arrays.stream(objects).forEach(object -> {
            if (object instanceof Exception) {
                args.add(((Exception) object).getMessage());
            } else {
                args.add(object.toString());
            }
        });
        return MessageFormat.format(format, args.toArray());
    }
    public synchronized void logLocalizedError(final String key, final Object... objects) {
        System.err.println(makeMessage(key, objects));
    }

    public synchronized void logLocalizedMessage(final String key, final Object... objects) {
        System.out.println(makeMessage(key, objects));
    }

    public void log(final String message) {
        System.out.println(message);
    }

    public synchronized void logLocalizedException(final Exception e) {
        System.err.println(e.getMessage());
    }
}
