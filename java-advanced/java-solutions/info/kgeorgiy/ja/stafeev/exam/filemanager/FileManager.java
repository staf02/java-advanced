package info.kgeorgiy.ja.stafeev.exam.filemanager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileManager {

    private Path currentDir;

    public FileManager() {
        currentDir = Path.of(System.getProperty("user.dir"));
    }

    private static void logSuccess(final String command) {
        System.out.println(command + " : success");
    }

    public void dir() {
        System.out.println("=== Content of " + currentDir.toString() + " ===");
        Arrays.stream(Objects.requireNonNull(currentDir.toFile().listFiles())).map(Object::toString).sorted().forEach(System.out::println);
    }

    public void rm(final String file) throws FileManagerException {
        final Path filePath = getPath(file);
        try {
            if (!Files.deleteIfExists(filePath)) {
                throw new FileManagerException("rm : no such file or directory " + file);
            } else {
                System.out.println("rm : file was removed : " + file);
            }
        } catch (final IOException e) {
            throw new FileManagerException("rm : error while trying to remove file : " + file);
        } catch (final SecurityException e) {
            throw new FileManagerException("security error");
        }
    }

    public void cd(final String path) throws FileManagerException {
        final Path newPath = getPath(path);
        if (Files.isDirectory(newPath)) {
            currentDir = newPath;
        } else {
            throw new FileManagerException("cd : not a directory" + path);
        }
    }

    public void create(final String file) throws FileManagerException {
        final Path filePath = getPath(file);
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                System.out.println("create : file was successfully created : " + file);
            } catch (final IOException e) {
                throw new FileManagerException("create : cannot create file " + filePath + " : " + e.getMessage());
            }
        } else {
            if (Files.isDirectory(filePath)) {
                throw new FileManagerException("create : " + filePath + " is directory");
            } else {
                throw new FileManagerException("create : file already exists : " + filePath);
            }
        }
    }

    public void mkdir(final String dir) throws FileManagerException {
        final Path dirPath = getPath(dir);
        try {
            Files.createDirectories(dirPath);
            System.out.println("mkdir : directory was successfully created : " + dir);
        } catch (final IOException e) {
            throw new FileManagerException("mkdir : cannot create directory : " + dirPath);
        } catch (final SecurityException e) {
            throw new FileManagerException("security error");
        }
    }

    public void rmdir(final String dir) throws FileManagerException {
        final Path dirPath = getPath(dir);
        final DeleteVisitor deleteVisitor = new DeleteVisitor();
        try {
            if (Files.exists(dirPath)) {
                Files.walkFileTree(dirPath, deleteVisitor);
                System.out.println("mkdir : directory was successfully removed : " + dir);
            } else {
                throw new FileManagerException("rmdir : Cannot delete temp folder : directory"  + dir + " does not exist");
            }
        } catch (final IOException e) {
            throw new FileManagerException("Cannot delete temp folder : " + e.getMessage());
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
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            final FileManager fileManager = new FileManager();
            String command;
            while ((command = in.readLine()) != null) {
                String[] tokens = command.split(" ");
                try {
                    switch (tokens[0]) {
                        case "dir" -> {
                            if (checkArgs(tokens, 1, "dir")) {
                                fileManager.dir();
                            }
                        }
                        case "rm" -> {
                            if (checkArgs(tokens, 2, "rm")) {
                                fileManager.rm(tokens[1]);
                            }
                        }
                        case "cd" -> {
                            if (checkArgs(tokens, 2, "cd")) {
                                fileManager.cd(tokens[1]);
                            }
                        }
                        case "create" -> {
                            if (checkArgs(tokens, 2, "create")) {
                                fileManager.create(tokens[1]);
                            }
                        }
                        case "mkdir" -> {
                            if (checkArgs(tokens, 2, "mkdir")) {
                                fileManager.mkdir(tokens[1]);
                            }
                        }
                        case "rmdir" -> {
                            if (checkArgs(tokens, 2, "rmdir")) {
                                fileManager.rmdir(tokens[1]);
                            }
                        }
                        default -> System.err.println("Unknown command : " + tokens[0]);
                    }
                } catch (final FileManagerException e) {
                    System.err.println(e.getMessage());
                } catch (final SecurityException e) {
                    System.out.println("security error");
                }
            }
        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private Path getPath(final String path) throws FileManagerException {
        try {
            Path newPath = Path.of(path);
            if (path.equals("..")) {
                newPath = currentDir.getParent();
                if (newPath == null) {
                    newPath = currentDir;
                }
            } else if (path.equals(".")) {
                newPath = currentDir;
            } else {
                newPath = currentDir.resolve(newPath);
            }
            return newPath;
        } catch (final InvalidPathException e) {
            throw new FileManagerException("Invalid path : " + path);
        }
    }
}
