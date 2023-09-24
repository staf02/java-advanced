package info.kgeorgiy.ja.stafeev.hello;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPUtils {

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final int IO_TIMEOUT = 300;
    public static void shutdownAndAwaitTermination(final ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static int parseIntWithoutLeadingZero(final String s) {
        if (s.length() > 1 && s.charAt(0) == '0') {
            throw new NumberFormatException("Founded leading zero in '" + s + "'");
        }

        return Integer.parseInt(s);
    }

    private static List<Integer> findNumbers(final String s) {
        int startIndex = -1;
        final List<Integer> res = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                if (startIndex == -1) {
                    startIndex = i;
                }
            } else if (startIndex != -1) {
                try {
                    res.add(parseIntWithoutLeadingZero(s.substring(startIndex, i)));
                } catch (final NumberFormatException ignored) {

                }
                startIndex = -1;
            }
        }
        if (startIndex != -1) {
            try {
                res.add(parseIntWithoutLeadingZero(s.substring(startIndex)));
            } catch (final NumberFormatException ignored) {

            }
        }
        return res;
    }

    public static boolean checkRelation(final String receivedData, final int threadNumber, final int requestNumber) {
        final List<Integer> nums = findNumbers(receivedData);
        if (nums.size() != 2) {
            return false;
        }
        return nums.get(0) == threadNumber && nums.get(1) == requestNumber;
    }

    public static void logError(final String message, final Exception e) {
        System.err.println(message + " : " + e.getMessage());
    }

    public static String getClientMessage(final String prefix, final int thread, final int requestNum) {
        return String.format(prefix + "%d_%d", thread, requestNum);
    }

    public static String getString(final ByteBuffer buffer) throws CharacterCodingException {
        return CHARSET.newDecoder().decode(buffer).toString();
    }

    public static void serverMain(String[] args) {

    }
}
