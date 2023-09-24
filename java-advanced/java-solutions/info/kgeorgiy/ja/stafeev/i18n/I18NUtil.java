package info.kgeorgiy.ja.stafeev.i18n;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class I18NUtil {
    public static void logError(final String message) {
        System.err.println(message);
    }

    public static void logError(final String message, final Exception e) {
        System.err.println(message + " : " + e.getMessage());
    }

    public static Path tryGetPath(final String file) {
        try {
            return Paths.get(file);
        } catch (final InvalidPathException e) {
            logError("Invalid path", e);
            return null;
        }
    }
}
