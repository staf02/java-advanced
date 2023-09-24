package info.kgeorgiy.ja.stafeev.exam.udptictactoe;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String decode(final DatagramPacket datagramPacket) {
        return new String(datagramPacket.getData(),
                datagramPacket.getOffset(),
                datagramPacket.getLength(),
                StandardCharsets.UTF_8);
    }

    public static void shutdownAndAwaitTermination(final ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (final InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
