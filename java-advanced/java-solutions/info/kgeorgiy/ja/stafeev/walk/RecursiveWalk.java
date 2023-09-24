package info.kgeorgiy.ja.stafeev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;

public class RecursiveWalk {
	public static Path tryGetPath(final String file) throws WalkException {
		try {
			return Paths.get(file);
		} catch (final InvalidPathException e) {
			throw new WalkException("Invalid path: " + e.getMessage());
		}
	}

	public static void walk(final String[] args, final boolean recursive) throws WalkException {
		if (args == null) {
			throw new WalkException("args[] is null");
		}
		if (args.length != 2) {
			throw new WalkException("Invalid count of arguments : expected 2, but found " + args.length);
		}
		if (args[0] == null || args[1] == null) {
			throw new WalkException("One of the arguments is null");
		}

		final Path inputPath = tryGetPath(args[0]);
		final Path outputPath = tryGetPath(args[1]);
		final Path outputParentPath = outputPath.getParent();
		if (outputParentPath != null) {
			try {
				Files.createDirectories(outputParentPath);
			} catch (final FileAlreadyExistsException e) {
				System.err.println("The directory name exists but is not a directory.");
			} catch (final IOException ignored) {
			}
		}

		try (final BufferedReader reader = Files.newBufferedReader(inputPath)) {
			try (final BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
				try {
					final HashVisitor visitor = new HashVisitor(writer);
					String inputLine;
					while ((inputLine = reader.readLine()) != null) {
						try {
							final Path currentPath = Paths.get(inputLine);
							try {
								if (recursive) {
									Files.walkFileTree(currentPath, visitor);
								} else {
									visitor.visitFile(currentPath, null);
								}
							} catch (final UncheckedIOException e) {
								throw new WalkException(e.getMessage());
							}
						} catch (final InvalidPathException e) {
							System.err.println(e.getMessage());
							HashCounter.writeInvalidHash(inputLine, writer);
						}
					}
				} catch (final IOException e) {
					throw new WalkException("Error while reading from " + inputPath + " : " + e.getMessage());
				}
			} catch (final IOException e) {
				throw new WalkException("Cannot open " + outputPath + " : " + e.getMessage());
			}
		} catch (final IOException e) {
			throw new WalkException("Cannot open " + inputPath + " : " + e.getMessage());
		}
	}

	public static void tryWalk(String[] args, boolean recursive) {
		try {
			walk(args, recursive);
		} catch (WalkException e) {
			System.err.println(e.getMessage());
		}
	}
	public static void main(String[] args) {
		tryWalk(args, true);
	}
}
