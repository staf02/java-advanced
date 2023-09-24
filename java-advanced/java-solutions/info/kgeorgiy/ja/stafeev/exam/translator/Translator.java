package info.kgeorgiy.ja.stafeev.exam.translator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Translator {

    public static void main(final String[] args) {
        if (args == null || args.length != 3) {
            System.err.println("error");
            return;
        }
        Path dictionaryPath, inputPath, outputPath;
        try {
            dictionaryPath = Path.of(args[0]);
            inputPath = Path.of(args[1]);
            outputPath = Path.of(args[2]);
        } catch (final InvalidPathException e) {
            System.err.println("Invalid path : " + e.getMessage());
            return;
        }
        Dictionary dictionary = new Dictionary();
        try (final BufferedReader reader = Files.newBufferedReader(dictionaryPath)) {
            String input;
            try {
                while ((input = reader.readLine()) != null) {
                    dictionary.addString(input.substring(0, input.indexOf(" | ")).toLowerCase(),
                            input.substring(input.indexOf(" | ") + 3, input.length()).toLowerCase());
                }
            } catch (final IOException e) {
                System.err.println("Cannot read from file");
                return;
            }
        } catch (final IOException e) {
            System.err.println("Cannot open file");
            return;
        }
        try (final BufferedReader reader = Files.newBufferedReader(inputPath)) {
            try (final BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                String input;
                try {
                    while ((input = reader.readLine()) != null) {
                        try {
                            dictionary.translateText(input.toLowerCase(), writer);
                        } catch (final IOException e) {
                            System.err.println("Cannot write to file");
                            return;
                        }
                    }
                } catch (final IOException e) {
                    System.err.println("Cannot read from file");
                }
            } catch (final IOException e) {
                System.err.println("Cannot open output file");
            }
        } catch (final IOException e) {
            System.err.println("Cannot open input file");
        }
    }
}
