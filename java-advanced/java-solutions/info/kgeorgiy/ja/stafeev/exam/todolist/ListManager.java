package info.kgeorgiy.ja.stafeev.exam.todolist;

import info.kgeorgiy.ja.stafeev.utils.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

public class ListManager {
    private final Map<Integer, WhatToDo> toDos;
    private final Logger logger;

    ListManager(final Logger logger) {
        this.logger = logger;
        toDos = new HashMap<>();
    }
    private void addWhatToDo(final String string) throws ListManagerException {
        try {
            final int priority;
            priority = Integer.parseInt(string.substring(0, string.indexOf('.')));
            final String whatToDo = string.substring(string.indexOf('.') + 1);
            toDos.put(priority, new WhatToDo(whatToDo, false));
        } catch (final NumberFormatException | StringIndexOutOfBoundsException e ) {
            throw new ListManagerException(logger.makeMessage("bad-format", e, "expected <Num>.<Task>"));
        }

    }

    private void run(final Path input, final Path output) {

        try (final BufferedReader in = Files.newBufferedReader(input)) {
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    try {
                        addWhatToDo(line);
                    } catch (final ListManagerException e) {
                        logger.logLocalizedException(e);
                    }
                }
            } catch (final IOException e) {
                logger.logLocalizedError("io-exception-reading", e);
                return;
            }
        } catch (final IOException e) {
            logger.logLocalizedError("file-not-found", e);
            return;
        }

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;

            while ((line = in.readLine()) != null) {

                final String command, data;
                try {
                    command = line.substring(0, line.indexOf(' '));
                    data = line.substring(line.indexOf(' ') + 1);
                }
                catch (final StringIndexOutOfBoundsException e) {
                    logger.logLocalizedError("cannot-parse-input");
                    continue;
                }

                try {
                    switch (command) {
                        case "add" -> addWhatToDo(data);
                        case "rm" -> {
                            try {
                                int prior = Integer.parseInt(data);
                                if (toDos.remove(prior) == null) {
                                    logger.logLocalizedError("prior-is-not-in-map");
                                } else {
                                    logger.logLocalizedMessage("removed");
                                }
                            } catch (final NumberFormatException e) {
                                logger.logLocalizedError("prior-not-number");
                            }
                        }
                        case "done" -> {
                            try {
                                int prior = Integer.parseInt(data);
                                WhatToDo toDo = toDos.get(prior);
                                if (toDos.get(prior) == null) {
                                    logger.logLocalizedError("prior-is-not-in-map");
                                } else {
                                    if (toDo.isDone()) {
                                        logger.logLocalizedError("already-done");
                                    } else {
                                        toDo.setDone();
                                        logger.logLocalizedMessage("changed-done");
                                    }
                                }
                            } catch (final NumberFormatException e) {
                                logger.logLocalizedError("prior-not-number");
                            }
                        }
                        case "print" -> {
                            boolean done;
                            if (data.equals("0")) {
                                done = false;
                            } else if (data.equals("1")) {
                                done = true;
                            } else {
                                logger.logLocalizedError("0-1");
                                continue;
                            }

                            for (final Map.Entry<Integer, WhatToDo> entry : toDos.entrySet()) {
                                if (entry.getValue().isDone() == done) {
                                    System.out.println(entry.getValue().toString());
                                }
                            }
                        }
                        default -> logger.logLocalizedError("not-available");
                    }
                } catch (final ListManagerException e) {
                    logger.logLocalizedException(e);
                }
            }
        } catch (final IOException e) {
            logger.logLocalizedError("io-exception-reading");
            return;
        }

        try (final BufferedWriter out = Files.newBufferedWriter(output)) {
            try {
                for (final Map.Entry<Integer, WhatToDo> entry : toDos.entrySet()) {
                    out.write(String.format("%d. %s%n", entry.getKey(), entry.getValue().toString()));
                }
            } catch (final IOException exception) {
                logger.logLocalizedError("io-exception-writing", exception);
            }
        } catch (IOException exception) {
            logger.logLocalizedError("file-not-found", exception);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 3) {
            System.err.println("Usage: ListManager <input file> <output file> <locale>");
            return;
        }

        final Locale locale = Locale.forLanguageTag(args[0]);
        final ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.stafeev.exam.todolist.ToDoListBundle", locale);
        } catch (final MissingResourceException e) {
            System.err.println("Supported only en_US and ru_RU");
            return;
        }

        Logger logger = new Logger(bundle);

        try {
            final Path inputFile = Path.of(args[1]);
            final Path outputFile = Path.of(args[2]);

            new ListManager(logger).run(inputFile, outputFile);
        } catch (final InvalidPathException e) {
            logger.logLocalizedError("invalid-path", e);
        }

    }
}
