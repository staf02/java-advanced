package info.kgeorgiy.ja.stafeev.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class HashVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter writer;
    private final HashCounter counter;

    public HashVisitor(final BufferedWriter writer) throws WalkException {
        this.writer = writer;
        this.counter = new HashCounter();
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
        try {
            return HashCounter.writeHash(file.toString(), counter.countHash(file), writer);
        }
        catch (WalkException e) {
            System.err.println(e.getMessage());
            return HashCounter.writeInvalidHash(file.toString(), writer);
        }
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
        return HashCounter.writeInvalidHash(file.toString(), writer);
    }

}
