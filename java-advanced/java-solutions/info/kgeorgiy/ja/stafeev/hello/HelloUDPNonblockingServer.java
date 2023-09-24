package info.kgeorgiy.ja.stafeev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.stafeev.hello.UDPUtils.*;

public class HelloUDPNonblockingServer implements HelloServer {

    private ExecutorService executorService;
    private final Queue<NonblockingUDPPacket> readable = new ConcurrentLinkedQueue<>();
    private final Queue<NonblockingUDPPacket> writable = new ConcurrentLinkedQueue<>();
    private Selector selector;
    private DatagramChannel datagramChannel;


    @Override
    public void start(final int port, final int threads) {
        try {
            selector = Selector.open();
            datagramChannel = DatagramChannel.open();

            datagramChannel.configureBlocking(false);
            datagramChannel.register(selector, SelectionKey.OP_READ);
            datagramChannel.bind(new InetSocketAddress(port));

            final int bufferSize = datagramChannel.socket().getReceiveBufferSize();

            IntStream.range(0, threads).
                forEach(i -> readable.add(new NonblockingUDPPacket(ByteBuffer.allocate(bufferSize), null)));

            executorService = Executors.newFixedThreadPool(threads + 1);
            executorService.submit(() -> {
                    while (!Thread.interrupted() && selector.isOpen()) {
                        try {
                            selector.select();
                        } catch (final IOException ex) {
                            logError("Selector is unavailable", ex);
                        }
                        for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                            final SelectionKey key = i.next();
                            try {
                                if (key.isReadable()) {
                                    sendReadable(key);
                                }
                                if (key.isWritable()) {
                                    sendWritable(key);
                                }
                            } finally {
                                i.remove();
                            }
                        }
                    }
                }
            );
        } catch (final IOException e) {
            logError("Error while trying to setup reciever", e);
        }
    }


    private NonblockingUDPPacket getPacket(SelectionKey key, int op, Queue<NonblockingUDPPacket> queue) {
        NonblockingUDPPacket packet = queue.remove();
        if (queue.isEmpty()) {
            key.interestOpsAnd(~op);
            selector.wakeup();
        }
        return packet;
    }

    private void addPacketToQueue(NonblockingUDPPacket packet, SelectionKey key, int op, Queue<NonblockingUDPPacket> queue) {
        queue.add(packet);
        key.interestOpsOr(op);
        selector.wakeup();
    }

    private static ByteBuffer getAnswer(ByteBuffer buffer) {
        buffer.flip();
        String request = "Hello, " + CHARSET.decode(buffer);
        return ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8));
    }

    private void sendReadable(SelectionKey key) {
        try {
            final NonblockingUDPPacket packet = getPacket(key, SelectionKey.OP_READ, readable);
            final ByteBuffer byteBuffer = packet.getBuffer().clear();
            final SocketAddress socketAddress = ((DatagramChannel) key.channel()).receive(byteBuffer);
            packet.setAddress(socketAddress);
            packet.setBuffer(getAnswer(byteBuffer));
            addPacketToQueue(packet, key, SelectionKey.OP_WRITE, writable);
        } catch (final IOException e) {
            logError("Error while trying to send readable", e);
        }
    }

    private void sendWritable(final SelectionKey key) {
        NonblockingUDPPacket packet = getPacket(key, SelectionKey.OP_WRITE, writable);
        try {
            ((DatagramChannel) key.channel()).send(packet.getBuffer(), packet.getAddress());
        } catch (final IOException e) {
            logError("Socket address is unavailable: " + packet.getAddress(), e);
        }
        addPacketToQueue(packet, key, SelectionKey.OP_READ, readable);
    }

    @Override
    public void close() {
        try {
            selector.close();
        } catch (final IOException e) {
            logError("Selector can't be closed", e);
        }
        try {
            datagramChannel.close();
        } catch (final IOException e) {
            logError("Canal can't be closed", e);
        }
        shutdownAndAwaitTermination(executorService);
    }
}
