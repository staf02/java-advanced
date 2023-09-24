package info.kgeorgiy.ja.stafeev.exam.fileconversion;


import info.kgeorgiy.ja.stafeev.utils.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

public class FileConversion {

    private final Logger logger;

    FileConversion(final Logger logger) {
        this.logger = logger;
    }

    private void encode(final Path inputPath,
                        final Charset inputCharset,
                        final Path outputPath,
                        final Charset outputCharset) throws FileConversionException {
        try (final BufferedReader reader = Files.newBufferedReader(inputPath, inputCharset)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, outputCharset)) {
                final char[] buffer = new char[2048];
                int i;
                try {
                    while ((i = (reader.read(buffer))) != -1) {
                        try {
                            writer.write(buffer, 0, i);
                        } catch (final IOException e) {
                            throw new FileConversionException(logger.makeMessage("error-writing", e));
                        }
                    }
                } catch (final IOException e) {
                    throw new FileConversionException(logger.makeMessage("error-reading", e));
                }
            } catch (final IOException e) {
                throw new FileConversionException(logger.makeMessage("error-open", e));
            } catch (final SecurityException e) {
                throw new FileConversionException(logger.makeMessage("security-error", e));
            }
        } catch (final IOException e) {
            throw new FileConversionException(logger.makeMessage("error-open", e));
        } catch (final SecurityException e) {
            throw new FileConversionException(logger.makeMessage("security-error", e));
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Bad usage");
            return;
        }
        
        final Set<Charset> supportedCharsets = Set.of(
                StandardCharsets.UTF_8,
                StandardCharsets.UTF_16,
                Charset.forName("cp1251"),
                Charset.forName("KOI8-R"),
                Charset.forName("Cp866")
        );
        
        final Locale locale = Locale.forLanguageTag(args[0]);
        final ResourceBundle resourceBundle;
        try {
            resourceBundle = ResourceBundle.getBundle(
                    "info.kgeorgiy.ja.stafeev.exam.fileconversion.FileConversionBundle", locale);
        } catch (final MissingResourceException e) {
            System.err.println("Couldn't find bundles");
            return;
        }
        final Logger logger = new Logger(resourceBundle);
        final Path inputPath, outputPath;
        try {
            inputPath = Path.of(args[1]);
            outputPath = Path.of(args[3]);
        } catch (final InvalidPathException e) {
            logger.logLocalizedError("invalid-path", e);
            return;
        }
        final Path outputParentPath = outputPath.getParent();
        if (outputParentPath != null) {
            try {
                Files.createDirectories(outputParentPath);
            } catch (final FileAlreadyExistsException e) {
                logger.logLocalizedError("directory-error", e);
            } catch (final IOException ignored) {
            }
        }
        final Charset inputCharset;
        final Charset outputCharset;
        try {
            inputCharset = Charset.forName(args[2]);
            outputCharset = Charset.forName(args[4]);
        } catch (final UnsupportedCharsetException e) {
            logger.logLocalizedError("unknown-charset", e);
            return;
        }
        if (!supportedCharsets.contains(inputCharset)) {
            logger.logLocalizedError("unsupported-charset", inputCharset);
            return;
        }
        if (!supportedCharsets.contains(outputCharset)) {
            logger.logLocalizedError("unsupported-charset", outputCharset);
            return;
        }
        try {
            new FileConversion(logger).encode(inputPath, inputCharset, outputPath, outputCharset);
            logger.logLocalizedMessage("success");
        } catch (final FileConversionException e) {
            logger.logLocalizedException(e);
        }
    }
}