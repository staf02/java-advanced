package info.kgeorgiy.ja.stafeev.exam.sort;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Sort {
    private static final int bucketSize = 1024 * 1024 * 128;
    private static final byte[] buffer = new byte[bucketSize];

    // Only UTF-8!!
    private static int lastIndexOfSpace(int bytes) {
        for (int i = bytes - 1; i > -1; --i) {
            if (buffer[i] == (int) ' ') {
                return i;
            }
        }

        if (bytes == bucketSize) {
            throw new IllegalArgumentException("Element is bigger than 128 Megabytes");
        }
        return 0;
    }

    private static void deleteAllTempFiles(final File file) {
        Arrays.stream(Objects.requireNonNull(file.getParentFile().listFiles((File dir, String name) -> name.matches(file.getName() + "[.]\\d+")))).forEach(File::deleteOnExit);
    }

    private static String firstNonNull(final List<String> strings) {
        for (final String s : strings) {
            if (s != null) {
                return s;
            }
        }

        return null;
    }

    public static void sort(final File file) {
        final long buckets = file.length() / bucketSize + (file.length() % bucketSize > 0 ? 1 : 0);
        if (buckets == 0) {
            return;
        }

        final List<File> bucketFiles = new ArrayList<>();

        // split big data into buckets files
        int toNextBucketBytes = 0;
        byte[] toNextBucket = new byte[bucketSize];
        try (final FileInputStream fis = new FileInputStream(file); final BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytes, currBucket = 0;
            while ((bytes = bis.read(buffer)) > 0) {
                String currBucketFileName = String.format("%s%05d", file.getName(), currBucket++);
                File currBucketFile = new File(file.getParent(), currBucketFileName);
                bucketFiles.add(currBucketFile);

                final int lastIndex = lastIndexOfSpace(bytes);
                try (FileOutputStream out = new FileOutputStream(currBucketFile)) {
                    if (toNextBucketBytes > 0) {
                        out.write(toNextBucket, 0, toNextBucketBytes);
                    }
                    out.write(' ');
                    out.write(buffer, 0, lastIndex - 1);
                }

                // copy next data to array
                toNextBucketBytes = bytes - lastIndex - 1;
                for (int i = 0; i < toNextBucketBytes; ++i) {
                    toNextBucket[i] = buffer[i + lastIndex + 1];
                }
            }
        } catch (final IllegalArgumentException e) {
            System.err.println(e.getMessage());
            deleteAllTempFiles(file);
            return;
        } catch (final FileNotFoundException e) {
            System.err.println("Cannot find file " + file.getName());
            return;
        } catch (final IOException exception) {
            System.err.println("An IOException was thrown at reading part");
            exception.printStackTrace();
            deleteAllTempFiles(file);
            return;
        }

        // mergesort in all buckets
        final List<Integer> bucketLens = new ArrayList<>();
        int arraySize = 0;
        for (final File bucketFile : bucketFiles) {
            final String[] arrayToWrite;

            // read from file and sort
            try (final BufferedReader in = Files.newBufferedReader(bucketFile.toPath(), StandardCharsets.UTF_8)) {
                final String[] elements = in.readLine().split(" ");
                Arrays.sort(elements);
                arrayToWrite = elements;
            } catch (IOException exception) {
                System.err.println("An IOException was thrown at mergesort part");
                exception.printStackTrace();
                deleteAllTempFiles(file);
                return;
            }

            bucketLens.add(arrayToWrite.length);
            arraySize += arrayToWrite.length;

            // write result into bucket files
            try (final BufferedWriter out = Files.newBufferedWriter(bucketFile.toPath(), StandardCharsets.UTF_8)) {
                for (final String s : arrayToWrite) {
                    out.write(String.format("%s%n", s));
                }
            } catch (IOException exception) {
                System.err.println("An IOException was thrown at writing sorted array part");
                exception.printStackTrace();
                deleteAllTempFiles(file);
                return;
            }
        }

        // collect all BufferedReaders
        List<BufferedReader> bucketReaders = new ArrayList<>();
        for (File bucketFile : bucketFiles) {
            try {
                bucketReaders.add(Files.newBufferedReader(bucketFile.toPath(), StandardCharsets.UTF_8));
            } catch (IOException exception) {
                System.err.println("An IOException was thrown while gathering BufferedReaders");
                exception.printStackTrace();
                deleteAllTempFiles(file);
                return;
            }
        }

        // writing result into new file
        File res = new File(file.getParent(), file.getName() + "-sorted");
        try (final BufferedWriter out = Files.newBufferedWriter(res.toPath(), StandardCharsets.UTF_8)) {
            List<String> elements = new ArrayList<>();
            List<Integer> positions = new ArrayList<>();
            for (final BufferedReader bucketReader : bucketReaders) {
                elements.add(bucketReader.readLine());
                positions.add(0);
            }

            for (int iterations = 0; iterations < arraySize; ++iterations) {
                String minVal = Objects.requireNonNull(firstNonNull(elements));
                int minPos = 0;
                for (int i = 0; i < positions.size(); ++i) {
                    if (minVal.compareTo(elements.get(i)) > 0) {
                        minVal = elements.get(i);
                        minPos = i;
                    }
                }

                out.write(minVal);
                out.write(' ');
                if (positions.get(minPos) < bucketLens.get(minPos)) {
                    positions.set(minPos, positions.get(minPos) + 1);
                    elements.set(minPos, bucketReaders.get(minPos).readLine());
                } else {
                    elements.set(minPos, null);
                }
            }

            System.out.println("Successfully sorted!");
        } catch (final NullPointerException e) {
            System.err.println("Produced NPE");
            e.printStackTrace();
        } catch (final IOException exception) {
            System.err.println("An IOException was thrown in writing sorted array (result)");
            exception.printStackTrace();
        } finally {
            //deleteAllTempFiles(file);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            System.err.println("Usage: Sorter <input file>");
            return;
        }

        sort(new File(args[0]));
    }
}
