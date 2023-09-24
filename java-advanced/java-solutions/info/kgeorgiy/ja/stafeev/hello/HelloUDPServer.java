package info.kgeorgiy.ja.stafeev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.stafeev.hello.UDPUtils.shutdownAndAwaitTermination;

public class HelloUDPServer implements HelloServer {

    private DatagramSocket datagramSocket;
    private int receiveBufferSize;
    private ExecutorService executorService;

    @Override
    public void start(final int port, final int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        try {
            this.datagramSocket = new DatagramSocket(inetSocketAddress);
            this.receiveBufferSize = datagramSocket.getReceiveBufferSize();
            IntStream.range(0, threads).forEach(i -> executorService.submit(this::receive));
        } catch (final SocketException e) {
            System.err.println(e.getMessage());
        }
    }

    private void receive() {
        final DatagramPacket request = new DatagramPacket(new byte[receiveBufferSize], receiveBufferSize);
        final DatagramPacket response = new DatagramPacket(new byte[0], 0);
        while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                datagramSocket.receive(request);
            } catch (final IOException e) {
                //System.err.println(e.getMessage());
                continue;
            }

            final String ans = "Hello, " + new String(request.getData(),
                    request.getOffset(),
                    request.getLength(),
                    StandardCharsets.UTF_8);
            System.out.println(ans);
            response.setData(ans.getBytes());
            response.setSocketAddress(request.getSocketAddress());

            try {
                datagramSocket.send(response);
            } catch (final IOException e) {
               // System.err.println(e.getMessage());
                break;
            }
        }
    }

    @Override
    public void close() {
        datagramSocket.close();
        shutdownAndAwaitTermination(executorService);
    }
}
