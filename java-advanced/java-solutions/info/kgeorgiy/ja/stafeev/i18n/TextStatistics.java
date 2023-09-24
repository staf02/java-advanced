package info.kgeorgiy.ja.stafeev.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static info.kgeorgiy.ja.stafeev.i18n.I18NUtil.logError;
import static info.kgeorgiy.ja.stafeev.i18n.I18NUtil.tryGetPath;

public class TextStatistics {

    public static void main(String[] args) {
        if (args == null) {
            logError("Args is null");
            return;
        }
        if (args.length != 4) {
            logError("Count of arguments is not 4");
            return;
        }
        for (String s : args) {
            if (s == null) {
                logError("One of the arguments is null");
                return;
            }
        }
        final Locale inputLocale = Locale.forLanguageTag(args[0]);
        final Locale outputLocale = Locale.forLanguageTag(args[1]);
        final Path inputPath = tryGetPath(args[2]);
        final Path outputPath = tryGetPath(args[3]);
        if (inputPath == null || outputPath == null) {
            return;
        }
        final Path outputParentPath = outputPath.getParent();
        if (outputParentPath != null) {
            try {
                Files.createDirectories(outputParentPath);
            } catch (final FileAlreadyExistsException e) {
                System.err.println("The directory name exists but is not a directory.");
            } catch (final IOException ignored) {
                // Nothing happens right now.
            }
        }
        final String fileContent;
        try {
            fileContent = Files.readString(inputPath);
        } catch (IOException e) {
            logError("Cannot read from file", e);
            return;
        }

        Parser parser = new Parser(inputPath, fileContent, inputLocale);
        List<String> sentences = parser.getSentences();
        List<String> words = parser.getWords();
        List<Number> numbers = parser.getNumbers();
        List<Number> moneys = parser.getMoneys();
        List<Date> dates = parser.getDates();
        return;
    }
}
