package info.kgeorgiy.ja.stafeev.exam.udpchat;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.stafeev.exam.udpchat.Utils.decode;
import static info.kgeorgiy.ja.stafeev.exam.udpchat.Utils.shutdownAndAwaitTermination;

public class Client {

    private final Queue<Message> messages;
    private final DatagramSocket datagramSocket;
    private final DatagramPacket request;
    private final ExecutorService executorService;
    private final Scanner scanner;
    private final int receiveSize;

    private String userName;
    public Client(final String serverAddress, final int port) throws UnknownHostException, SocketException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(serverAddress), port);
        this.datagramSocket = new DatagramSocket();
        this.request = new DatagramPacket(new byte[0], 0, address);
        this.receiveSize = datagramSocket.getReceiveBufferSize();
        this.executorService = Executors.newFixedThreadPool(8);
        this.messages = new ConcurrentLinkedDeque<>();
        this.scanner = new Scanner(System.in);
    }

    private void trySend() {
        try {
            datagramSocket.send(request);
        } catch (final IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Stopping app");
        }
    }

    public void start() {
        try {
            register();
        } catch (final Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("You have registered");
        System.out.println("Commands : r - read message, w - write message, s - sleep, c - close");

        IntStream.range(0, 8).forEach(i -> executorService.submit(this::receiveMessages));

        while (!datagramSocket.isClosed()) {
            int messagesCount = messages.size();
            System.out.println("You have " + messagesCount + " messages");
            System.out.println("Enter command:");
            final String cmd = scanner.nextLine();
            switch (cmd) {
                case "r" -> {
                    if (messagesCount > 0) {
                        System.out.println(messages.poll().clientMessage());
                    }
                } case "w" -> {
                    System.out.print("To: ");
                    final String to = scanner.nextLine();
                    final String text = scanner.nextLine();
                    Message message = new Message(to, text);
                    request.setData(message.toString().getBytes());
                    trySend();
                } case "c" -> {
                    request.setData(("LEAVE" + userName).getBytes());
                    trySend();
                    close();
                } case "s" -> {
                } default -> System.err.println(cmd + " - unknown command");
            }
        }
    }

    public void close() {
        datagramSocket.close();
        shutdownAndAwaitTermination(executorService);
    }

    private void register() throws IOException {
        final DatagramPacket response = new DatagramPacket(new byte[receiveSize], receiveSize);
        System.out.println("Enter your name");
        userName = scanner.nextLine();

        request.setData(("NEW" + userName).getBytes());
        datagramSocket.send(request);
        datagramSocket.receive(response);

        final String result = decode(response);
        if (!result.equals("REG_OK")) {
            throw new IOException("Registration failed");
        }
    }

    private void receiveMessages() {
        final DatagramPacket responseFromServer = new DatagramPacket(new byte[receiveSize], receiveSize);
        while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                datagramSocket.receive(responseFromServer);
            } catch (final IOException e) {;
                continue;
            }

            try {
                final Message message = new Message(decode(responseFromServer));
                messages.add(message);
            } catch (final IllegalArgumentException ignored) {

            }
        }
    }

    public static void main(final String[] args) {
        try {
            new Client("localhost", 8888).start();
        } catch (final UnknownHostException | SocketException e) {
            System.err.println(e.getMessage());
        }
    }
}
