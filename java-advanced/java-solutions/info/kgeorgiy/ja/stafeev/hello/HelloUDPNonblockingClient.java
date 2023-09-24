//package info.kgeorgiy.ja.stafeev.hello;
//
//import info.kgeorgiy.java.advanced.hello.HelloClient;
//
//import java.io.IOException;
//import java.net.*;
//import java.nio.Buffer;
//import java.nio.channels.DatagramChannel;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.IntStream;
//
//import static info.kgeorgiy.ja.stafeev.hello.UDPUtils.logError;
//
//public class HelloUDPNonblockingClient implements HelloClient {
//    private final List<DatagramChannel> channels = new ArrayList<>();
//    private String prefix;
//
//
//
//    @Override
//    public void run(String host, int port, String prefix, int threads, int requests) {
//        this.prefix = prefix;
//        final Buffer buffer;
//        try (final Selector selector = Selector.open()) {
//            final InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
//            IntStream.range(1, threads + 1).forEach(i -> {
//                try (final DatagramChannel channel = DatagramChannel.open()) {
//                    channels.add(channel);
//                    channel.configureBlocking(false);
//                    channel.connect(socketAddress);
//                    channel.register(selector, SelectionKey.OP_WRITE, );
//                } catch (final IOException e) {
//
//                }
//            });
//        } catch (final UnknownHostException e) {
//            logError("Unknown host", e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
