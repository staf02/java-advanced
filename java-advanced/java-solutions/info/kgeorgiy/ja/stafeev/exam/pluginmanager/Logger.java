package info.kgeorgiy.ja.stafeev.exam.pluginmanager;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Logger {
    private final ResourceBundle bundle;

    Logger(final ResourceBundle resourceBundle) {
        bundle = resourceBundle;
    }

    public String loadLocalizedError(final String key) {
        return bundle.getString(key);
    }

    public void logError(final String message, final Exception e) {
        // :NOTE: * String concatenation in i18n context
        System.err.println(message + " : " + e.getMessage());
    }

    public void logError(final Exception e) {
        System.err.println(e.getMessage());
    }

    public void logError(final String message) {
        System.err.println(message);
    }

    public void log(final String message) {
        System.out.println(message);
    }
}
