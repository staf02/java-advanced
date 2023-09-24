package info.kgeorgiy.ja.stafeev.exam.filemanager;

import java.nio.file.Path;

public class Main {
    public static void main(final String[] args) {
        final Path path = Path.of(System.getProperty("user.dir"));
        final Path root = path.getRoot();
        System.out.println(path);
        System.out.println(path.getParent());
    }
}
