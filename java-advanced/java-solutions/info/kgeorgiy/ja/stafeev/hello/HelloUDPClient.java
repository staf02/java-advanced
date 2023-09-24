package info.kgeorgiy.ja.stafeev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.stafeev.hello.UDPUtils.*;

public class HelloUDPClient implements HelloClient {
    private record ClientSender(int thread, String prefix, DatagramSocket datagramSocket, DatagramPacket request,
                                DatagramPacket response) {

        private void sendRequest(final int requestNum) {
                final String requestString = getClientMessage(prefix, thread, requestNum);
                request.setData(requestString.getBytes(StandardCharsets.UTF_8));
                while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    try {
                        datagramSocket.send(request);
                        datagramSocket.receive(response);
                        final String responseString = new String(response.getData(),
                                response.getOffset(),
                                response.getLength(),
                                StandardCharsets.UTF_8);
                        if (checkRelation(responseString, thread, requestNum)) {
                            break;
                        }
                    } catch (final IOException ignored) {
                    }
                }
            }

            private void sendSeries(final int requests) {
                IntStream.range(0, requests).forEach(i -> sendRequest(i + 1));
            }
        }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        try {
            final InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
            final ExecutorService executorService = Executors.newFixedThreadPool(threads);
            IntStream.range(0, threads).forEach(i -> executorService.submit(() -> {
                try (final DatagramSocket datagramSocket = new DatagramSocket()) {
                    datagramSocket.setSoTimeout(200);
                    final DatagramPacket request = new DatagramPacket(new byte[0], 0, socketAddress);
                    final DatagramPacket response = new DatagramPacket(new byte[datagramSocket.getReceiveBufferSize()],
                            datagramSocket.getReceiveBufferSize());
                    new ClientSender(i + 1, prefix, datagramSocket, request, response)
                            .sendSeries(requests);
                } catch (final SocketException e) {
                    System.err.println(e.getMessage());
                }
            }));
            UDPUtils.shutdownAndAwaitTermination(executorService);
        } catch (final UnknownHostException e) {
            logError("Unknown host", e);
        }
    }
}