package info.kgeorgiy.ja.stafeev.exam.udpproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class UDPProxy {
    private final ExecutorService executorService;
    private final DatagramSocket datagramSocket;
    private final int receiveSize;
    private final Map<SocketAddress, SocketAddress> addresses;
    private final int threads = 8;

    public UDPProxy() throws SocketException {
        this.executorService = Executors.newFixedThreadPool(threads);
        this.datagramSocket = new DatagramSocket(8888);
        datagramSocket.setSoTimeout(200);
        this.receiveSize = datagramSocket.getReceiveBufferSize();
        this.addresses = new HashMap<>();
    }

    public void parseProxyTableFromFile(final Path inputFile) throws IOException {
        final BufferedReader reader = Files.newBufferedReader(inputFile);
        String str;
        while ((str = reader.readLine()) != null) {
            final String[] tokens = str.split(" ");
            try {
                final int localPort = Integer.parseInt(tokens[0]);
                final String remoteIP = tokens[1];
                final int remotePort = Integer.parseInt(tokens[2]);
                final SocketAddress localAddress = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), localPort);
                final SocketAddress remoteAddress = new InetSocketAddress(InetAddress.getByName(remoteIP), remotePort);
                addresses.put(localAddress, remoteAddress);
            } catch (final NumberFormatException e) {
                throw new IOException("Bad table format");
            }
        }
    }

    private void process() {
        final DatagramPacket receivedPacket = new DatagramPacket(new byte[receiveSize], receiveSize);
        final DatagramPacket proxyPacket = new DatagramPacket(new byte[0], 0);
        while (!Thread.currentThread().isInterrupted() && !datagramSocket.isClosed()) {
            try {
                datagramSocket.receive(receivedPacket);
            } catch (final IOException e) {
                continue;
            }
            final SocketAddress socketAddress = receivedPacket.getSocketAddress();
            if (addresses.containsKey(socketAddress)) {
                proxyPacket.setData(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength());
                proxyPacket.setSocketAddress(addresses.get(socketAddress));
                try {
                    datagramSocket.send(proxyPacket);
                } catch (final IOException e) {
                    System.err.println("Cannot send request. Stop " + e.getMessage());
                    return;
                }
            } else {
                System.err.println("Unknown socket address " + socketAddress);
            }
        }
    }

    public void start() {
        for (int i = 0; i < threads; i++) {
            executorService.submit(this::process);
        }
    }

    public void close() {
        datagramSocket.close();
        shutdownAndAwaitTermination();
    }

    private void shutdownAndAwaitTermination() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (final InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(final String[] args) {
        try {
            final UDPProxy proxy = new UDPProxy();
            proxy.parseProxyTableFromFile(Path.of(args[0]));
            proxy.start();
        } catch (final SocketException e) {
            System.err.println("Cannot open socket : " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Error while parsing table : " + e.getMessage());
        }
    }
}
