package info.kgeorgiy.ja.stafeev.exam.udpchat;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.stafeev.exam.udpchat.Utils.decode;
import static info.kgeorgiy.ja.stafeev.exam.udpchat.Utils.shutdownAndAwaitTermination;

public class Server {

    private final DatagramSocket datagramSocket;
    private final ExecutorService receiveService;
    private final ExecutorService sendService;
    private final Map<SocketAddress, String> users;
    private final Map<String, SocketAddress> usersAddresses;
    private final BlockingQueue<MessageWithAddress> responses;
    private final int receiveSize;
    
    private record MessageWithAddress(String message, SocketAddress inetSocketAddress) {}

    Server(final int port) throws UnknownHostException, SocketException {
        final InetSocketAddress address = new InetSocketAddress(port);
        this.datagramSocket = new DatagramSocket(address);
        this.receiveSize = datagramSocket.getReceiveBufferSize();

        this.receiveService = Executors.newFixedThreadPool(8);
        this.sendService = Executors.newFixedThreadPool(8);

        this.users = new ConcurrentHashMap<>();
        this.usersAddresses = new ConcurrentHashMap<>();
        this.responses = new LinkedBlockingDeque<>();
    }
    
    private void send() {
        final DatagramPacket response = new DatagramPacket(new byte[0], 0);
        try {
            while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                final MessageWithAddress messageWithAddress = responses.take();
                response.setData(messageWithAddress.message.getBytes());
                response.setSocketAddress(messageWithAddress.inetSocketAddress);

                try {
                    datagramSocket.send(response);
                } catch (final IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (final InterruptedException e) {
            System.err.println(e.getMessage());
        } finally {
            Thread.currentThread().interrupt();
        }

    }

    private void receive() {
        final DatagramPacket request = new DatagramPacket(new byte[receiveSize], receiveSize);

        while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                datagramSocket.receive(request);
            } catch (final IOException e) {
                continue;
            }

            final String data = decode(request);
            final SocketAddress socketAddress = request.getSocketAddress();

            final String ans;
            final SocketAddress resultAddress;

            if (data.startsWith("USER")) {
                final Message message = new Message(data);
                resultAddress = usersAddresses.get(message.getUserName());
                message.setUserName(users.get(socketAddress));
                ans = message.toString();
            } else if (data.startsWith("NEW")) {
                final String userName = data.substring(3);
                if (usersAddresses.putIfAbsent(userName, socketAddress) == null) {
                    ans = "REG_OK";
                    for (final Map.Entry<SocketAddress, String> user : users.entrySet()) {
                        responses.add(new MessageWithAddress(
                                new Message(userName, userName + " joined the chat").toString(),
                                user.getKey()));
                    }
                    users.put(socketAddress, userName);
                } else {
                    ans = "REG_FAIL";
                }
                resultAddress = socketAddress;
            } else if (data.startsWith("LEAVE")) {
                final String userName = data.substring(5);
                final SocketAddress address = usersAddresses.get(userName);
                if (usersAddresses.remove(userName) != null) {
                    users.remove(address);
                    for (final Map.Entry<String, SocketAddress> user : usersAddresses.entrySet()) {
                        responses.add(new MessageWithAddress(
                                new Message(userName, userName + " left the chat").toString(),
                                user.getValue()));
                    }
                }
                continue;
            }
            else {
                System.err.println("Bad request : " + data);
                continue;
            }

            responses.add(new MessageWithAddress(ans, resultAddress));

        }
    }

    public void start() {
        System.out.println("Server started");
        IntStream.range(0, 8).forEach(i -> receiveService.submit(this::receive));
        IntStream.range(0, 8).forEach(i -> sendService.submit(this::send));
    }

    public void stop() {
        datagramSocket.close();
        shutdownAndAwaitTermination(receiveService);
        shutdownAndAwaitTermination(sendService);
    }

    public static void main(final String[] args) {
        try {
            new Server(8888).start();
        } catch (final UnknownHostException | SocketException e) {
            System.err.println(e.getMessage());
        }
    }
}
