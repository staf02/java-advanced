package info.kgeorgiy.ja.stafeev.exam.pluginmanager;

import info.kgeorgiy.ja.stafeev.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PluginManager {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"en-US"};
            System.out.println("There is no local. Set en-US");
        }
        Locale locale = Locale.forLanguageTag(args[0]);
        Logger logger;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.stafeev.exam.pluginmanager.PluginManagerBundle", locale);
            logger = new Logger(bundle);
        } catch (final MissingResourceException e) {
            System.err.println("Couldn't find bundles");
            return;
        }
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String inputLine;
            final Map<String, Plugin> methodMap = new HashMap<>();
            final Map<PluginId, Plugin> loaded = new HashMap<>();
            while ((inputLine = reader.readLine()) != null) {
                final String[] commandArgs = inputLine.split(" ");
                switch (commandArgs[0]) {
                    case "load" -> {
                        if (commandArgs.length != 4) {
                            logger.logLocalizedError("load-invalid-usage-message");
                            continue;
                        }
                        final String name = commandArgs[1];
                        final String className = commandArgs[2];
                        final String classPath = commandArgs[3];
                        final PluginId pluginId = new PluginId(className, classPath);
                        Plugin plugin = loaded.get(pluginId);
                        if (plugin == null) {
                            try {
                                plugin = new Plugin(className, classPath);
                                plugin.load(logger);
                                loaded.put(pluginId, plugin);
                            } catch (final PluginException e) {
                                logger.logLocalizedException(e);
                                continue;
                            }
                        }

                        if (methodMap.putIfAbsent(name, plugin) != null) {
                            logger.logLocalizedError("plugin-already-loaded-message");
                            continue;
                        }
                        logger.logLocalizedMessage("plugin-successfully-loaded-message");
                    }
                    case "run" -> {
                        if (commandArgs.length != 2) {
                            logger.logLocalizedError("run-invalid-usage-message");
                            continue;
                        }
                        final String name = commandArgs[1];
                        final Plugin plugin = methodMap.get(name);
                        if (plugin == null) {
                            logger.logLocalizedError("no-such-plugin-message");
                            continue;
                        }
                        try {
                            plugin.run();
                        } catch (final RuntimeException e) {
                            logger.logLocalizedError("runtime-error", e);
                        }
                    }
                    case "unload" -> {
                        if (commandArgs.length != 2) {
                            logger.logLocalizedError("unload-invalid-usage-message");
                            continue;
                        }
                        final String name = commandArgs[1];
                        if (methodMap.remove(name) == null) {
                            logger.logLocalizedError("unloaded-plugin-message");
                        } else {
                            logger.logLocalizedMessage("plugin-successfully-unloaded-message");
                        }
                    }
                    default -> logger.logLocalizedError("unknown-command-message");
                }
            }
        } catch (final IOException e) {
            logger.logLocalizedError("ioexception-message", e);
        }
    }
}
