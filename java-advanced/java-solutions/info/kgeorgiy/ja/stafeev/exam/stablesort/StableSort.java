package info.kgeorgiy.ja.stafeev.exam.stablesort;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

public class StableSort {

    private record ScriptString(String str, int time) implements Comparable<ScriptString> {

        @Override
        public int compareTo(final ScriptString o) {
            return Integer.compare(time, o.time);
        }
    }
    private final Map<Integer, List<ScriptString>> stringMap;
    private int time;

    public StableSort() {
        stringMap = new TreeMap<>();
        time = 0;
    }

    public void remove(final int index) {
        stringMap.remove(index);
    }

    public void add(final int index, final String string) {
        stringMap.putIfAbsent(index, new ArrayList<>());
        stringMap.get(index).add(new ScriptString(string, time++));
    }

    public void print() {
        for (final Map.Entry<Integer, List<ScriptString>> list : stringMap.entrySet()) {
            Collections.sort(list.getValue());
            for (final ScriptString string : list.getValue()) {
                System.out.println(list.getKey() + " " + string);
            }
        }
    }

    private static boolean checkArgs(final String[] args, final int size, final String command) {
        if (args.length < size) {
            System.err.println(command + " - too few arguments");
        } else if (args.length > size) {
            System.err.println(command + " - too many arguments");
        }
        return args.length == size;
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 1) {
            System.err.println("error");
        }
        Path inputPath;
        try {
            inputPath = Path.of(args[0]);
        } catch (final InvalidPathException e) {
            System.err.println("Invalid path : " + e.getMessage());
            return;
        }

        StableSort stableSort = new StableSort();

        try (final BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String input;
            try {
                while ((input = reader.readLine()) != null) {
                    String[] tokens = input.split(" ");
                    switch (tokens[0]) {
                        case "add" -> {
                            if (checkArgs(tokens, 3, "add")) {
                                try {
                                    int index = Integer.parseInt(tokens[1]);
                                    stableSort.add(index, tokens[2]);
                                } catch (final NumberFormatException e) {
                                    System.err.println(tokens[1] + " - not integer");
                                }
                            }
                        }
                        case "remove" -> {
                            if (checkArgs(tokens, 2, "remove")) {
                                try {
                                    int index = Integer.parseInt(tokens[1]);
                                    stableSort.remove(index);
                                } catch (final NumberFormatException e) {
                                    System.err.println(tokens[1] + " - not integer");
                                }
                            }
                        }
                        case "print" -> {
                            if (checkArgs(tokens, 1, "print")) {
                                stableSort.print();
                            }
                        }
                        default -> {
                            System.err.println("Unknown command");
                        }
                    }
                }
            } catch (final IOException e) {
                System.err.println("Cannot read from file");
            }
        } catch (final IOException e) {
            System.err.println("Cannot open file");
        }
    }
}
