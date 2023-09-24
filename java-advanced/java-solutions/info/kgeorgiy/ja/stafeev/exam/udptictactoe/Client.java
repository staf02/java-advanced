package info.kgeorgiy.ja.stafeev.exam.udptictactoe;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import static info.kgeorgiy.ja.stafeev.exam.udpchat.Utils.decode;

public class Client {

    private final DatagramSocket datagramSocket;
    private final DatagramPacket request;
    private final DatagramPacket response;
    private final Scanner scanner;
    private Mark mark;

    public Client(final String serverAddress, final int port) throws UnknownHostException, SocketException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(serverAddress), port);
        this.datagramSocket = new DatagramSocket();
        this.request = new DatagramPacket(new byte[0], 0, address);
        this.response = new DatagramPacket(new byte[datagramSocket.getReceiveBufferSize()],
                datagramSocket.getReceiveBufferSize());
        this.scanner = new Scanner(System.in);
    }

    public void makeTurn(final Game game) {
        int x = 0, y = 0;
        do {
            String command = scanner.nextLine();
            final String[] coords = command.split(" ");
            try {
                x = Integer.parseInt(coords[0]);
                y = Integer.parseInt(coords[1]);
            } catch (final NumberFormatException e) {
                System.err.println("Bad position. Correct format is x y, where x, y - numbers");
                continue;
            }
            if (!(x >= 1 && x <= 3 && y >= 1 && y <= 3)) {
                System.err.println("Bad coords : " + x + " " + y);
                continue;
            }
            if (!game.free(x - 1, y - 1)) {
                System.err.println("Cell with x = " + x + ", y = " + y + " is not free");
            }
        } while (!game.free(x - 1, y - 1));
        game.set(x - 1, y - 1, mark);
    }

    private void processGameResult(final GameResult gameResult) {
        if (gameResult != GameResult.NOT_FINISHED) {
            System.out.println("Game finished");
            if (gameResult == GameResult.DRAW) {
                System.out.println("Draw");
            } else if (gameResult == GameResult.X_WIN && mark == Mark.X) {
                System.out.println("You are winner");
            } else if (gameResult == GameResult.O_WIN && mark == Mark.O) {
                System.out.println("You are winner");
            } else {
                System.out.println("You are loser");
            }
        }
    }

    public void start() {
        try {
            register();
        } catch (final IOException e) {
            System.err.println(e.getMessage());
            return;
        }
        while (!datagramSocket.isClosed()) {
            final String data;
            try {
                datagramSocket.receive(response);
                data = decode(response);
            } catch (final IOException e) {
                System.err.println(e.getMessage());
                break;
            }
            if (data.equals("Game started")) {
                System.out.println(data);
                if (mark == Mark.O) {
                    System.out.println("Waiting for first turn");
                }
                continue;
            }
            Game game = Game.fromString(data);
            System.out.println(game.renderGame());
            if (game.gameResult() != GameResult.NOT_FINISHED) {
                processGameResult(game.gameResult());
                System.out.println("Do you want to play again?");
                String ans = scanner.nextLine();
                while (!ans.equalsIgnoreCase("Y") && !ans.equalsIgnoreCase("N")) {
                    ans = scanner.nextLine();
                }
                if (ans.equalsIgnoreCase("Y")) {
                    start();
                } else {
                    return;
                }
            }
            makeTurn(game);
            System.out.println(game.renderGame());
            if (game.gameResult() == GameResult.NOT_FINISHED) {
                System.out.println("Waiting for other player");
            }
            request.setData((game.toString()).getBytes());
            try {
                datagramSocket.send(request);
            } catch (final IOException e) {
                System.err.println(e.getMessage());
                break;
            }
        }

    }

    private void register() throws IOException {
        request.setData(("REG").getBytes());
        datagramSocket.send(request);
        datagramSocket.receive(response);
        final String type = decode(response);
        if (type.equals("F")) {
            throw new IOException("Registration failed");
        }
        switch (type) {
            case "X" -> {
                mark = Mark.X;
                System.out.println("You are playing as X");
            }
            case "O" -> {
                mark = Mark.O;
                System.out.println("You are playing as O");
            }
            default -> throw new RuntimeException("Bad type");
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
