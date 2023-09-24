package info.kgeorgiy.ja.stafeev.walk;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashCounter {

    private final byte[] buffer = new byte[1024];
    private final MessageDigest digest;

    public HashCounter() throws WalkException {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new WalkException("No Such Algorithm: SHA-256");
        }
    }

    public String countHash(final Path file) throws WalkException {
        digest.reset();
        try (BufferedInputStream reader = new BufferedInputStream(Files.newInputStream(file))) {
            int count;
            while ((count = reader.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (final IOException e) {
            throw new WalkException("Cannot open file " + file);
        }
    }

    public static FileVisitResult writeHash(final String file, final String hash, BufferedWriter writer) {
        try {
            writer.write(hash + " " + file);
            writer.newLine();
            return FileVisitResult.CONTINUE;
        } catch (final IOException e) {
           throw new UncheckedIOException(new IOException("Error while writing in " + file + " : " + e.getMessage()));
        }
    }

    public static FileVisitResult writeInvalidHash(final String file, BufferedWriter writer) {
        final String NULL_HASH = "0".repeat(64);
        return writeHash(file, NULL_HASH, writer);
    }
}
