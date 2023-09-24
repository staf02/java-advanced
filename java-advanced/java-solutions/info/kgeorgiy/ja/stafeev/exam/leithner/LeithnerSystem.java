package info.kgeorgiy.ja.stafeev.exam.leithner;

import info.kgeorgiy.ja.stafeev.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public class LeithnerSystem {
    private final List<Set<Card>> boxes;
    private final Scanner scanner;
    private final Random random;
    private final Logger logger;
    private final int BOXES_COUNT = 10;
    private final double SUM;
    LeithnerSystem(final Logger logger, final Scanner scanner, final Path inputPath) {
        this.logger = logger;
        this.boxes = new ArrayList<>(Collections.nCopies(BOXES_COUNT, new HashSet<>()));
        this.scanner = scanner;

        SUM = IntStream.range(1, 11).mapToDouble(i -> Math.pow(1.5, i)).sum();
        random = new Random();
    }

    private enum FileType {
        CACHE,
        INPUT_FILE
    }

    public void saveToCache(final Path path) {

    }

    public void readFromFile(final Path path, final FileType type) {
        try (final BufferedReader reader = Files.newBufferedReader(path)) {
            String s;
            try {
                while ((s = reader.readLine()) != null) {
                    final String[] tokens = s.split(" ");
                    final int index;
                    final String word;
                    final String translate;
                    if (tokens.length == 3 && type == FileType.CACHE) {
                        try {
                            index = Integer.parseInt(tokens[0]);
                            word = tokens[1];
                            translate = tokens[2];
                        } catch (final NumberFormatException e) {
                            System.err.println("Incorrect input string" + s);
                            continue;
                        }
                    } else if (tokens.length == 2 && type == FileType.INPUT_FILE) {
                        word = tokens[0];
                        translate = tokens[1];
                        index = 10;
                    } else {
                        System.err.println("Incorrect input string" + s);
                        continue;
                    }
                    boxes.get(index).add(new Card(word, translate));
                }
            } catch (final IOException e) {
                System.err.println("io-exception");
            }
        } catch (final IOException e) {
            System.err.println("io-exception");
        }
    }
    private int chooseRandomBox() {
        double randomNum = random.nextDouble(0, SUM);
        for (int i = 10; i >= 1; i--) {
            final double pow = Math.pow(1.5, i);
            if (randomNum < pow) {
                return i;
            }
            randomNum -= pow;
        }
        return 1;
    }

    private Card chooseRandomCard(final Set<Card> box) {
        final int size = box.size();
        final int item = new Random().nextInt(size);
        int i = 0;
        for (final Card obj : box) {
            if (i == item) {
                return obj;
            } else {
                i++;
            }
        }
        return null;
    }

    public void startGame() {
        Card card = null;
        int randomIndex = 0;
        while (card == null) {
            randomIndex = chooseRandomBox() - 1;
            card = chooseRandomCard(boxes.get(randomIndex));
        }
        System.out.println("Enter translate");
        final String translate = scanner.nextLine();
        final int newIndex;
        if (translate.equalsIgnoreCase(card.getTranslate())) {
            newIndex = Math.min(1, randomIndex - 1);
            System.out.println("OK");
        } else {
            newIndex = 9;
            System.out.println("FAIL");
        }
        boxes.get(randomIndex).remove(card);
        boxes.get(newIndex).add(card);
    }

    public void stopGame() {

    }

    public static void main(final String[] args) {
        final Path inputPath = Path.of("input.txt");
        LeithnerSystem leithnerSystem = new LeithnerSystem(null, null, inputPath);
        leithnerSystem.readFromFile(inputPath, FileType.INPUT_FILE);
        leithnerSystem.readFromFile(Path.of("cache.txt"), FileType.CACHE);
        leithnerSystem.startGame();
    }
}
