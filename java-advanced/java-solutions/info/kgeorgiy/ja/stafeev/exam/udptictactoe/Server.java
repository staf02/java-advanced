package info.kgeorgiy.ja.stafeev.exam.udptictactoe;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final DatagramSocket datagramSocket;
    private final DatagramPacket request;
    private final DatagramPacket response;
    private final Map<Mark, SocketAddress> players;

    Server(final int port) throws UnknownHostException, SocketException {
        final InetSocketAddress address = new InetSocketAddress(port);
        this.datagramSocket = new DatagramSocket(address);
        this.players = new HashMap<>();
        this.request = new DatagramPacket(new byte[datagramSocket.getReceiveBufferSize()], datagramSocket.getReceiveBufferSize());
        this.response = new DatagramPacket(new byte[0], 0);
    }

    private void registerPlayers() {
        players.clear();
        while (!datagramSocket.isClosed() && players.size() != 2) {
            try {
                datagramSocket.receive(request);
            } catch (final IOException e) {
                continue;
            }
            final String data = Utils.decode(request);
            final SocketAddress socketAddress = request.getSocketAddress();
            final String ans;
            if (data.equals("REG")) {
                if (players.containsKey(Mark.X)) {
                    players.put(Mark.O, socketAddress);
                    System.out.println("Second player joined the game");
                    ans = "O";
                } else {
                    players.put(Mark.X, socketAddress);
                    System.out.println("First player joined the game");
                    ans = "X";
                }
            } else {
                System.err.println("Error");
                ans = "F";
            }
            response.setData(ans.getBytes());
            response.setSocketAddress(socketAddress);
            try {
                datagramSocket.send(response);
            } catch (final IOException e) {
                throw new RuntimeException("Cannot send answer for player; Stopping server");
            }
        }
    }

    boolean checkIfPlayer() {
        final SocketAddress socketAddress = request.getSocketAddress();
        if (socketAddress.equals(players.get(Mark.X)) || socketAddress.equals(players.get(Mark.O))) {
            return true;
        }
        response.setData("F".getBytes());
        response.setSocketAddress(socketAddress);
        try {
            datagramSocket.send(response);
        } catch (final IOException e) {
            return false;
        }
        return false;
    }

    private void startGame() {
        for (final Map.Entry<Mark, SocketAddress> player : players.entrySet()) {
            response.setData("Game started".getBytes());
            response.setSocketAddress(player.getValue());
            try {
                datagramSocket.send(response);
            } catch (final IOException e) {
                throw new RuntimeException("Cannot send answer for player; Stopping server");
            }
        }
        Game game = new Game();
        while (!datagramSocket.isClosed()) {
            response.setData(game.toString().getBytes());
            System.out.println(game);
            response.setSocketAddress(players.get(game.getActivePlayer()));
            try {
                datagramSocket.send(response);
            } catch (final IOException e) {
                throw new RuntimeException("Cannot send answer for player; Stopping server");
            }
            try {
                do {
                    datagramSocket.receive(request);
                } while (!checkIfPlayer());
            } catch (final IOException e) {
                throw new RuntimeException("Cannot send answer for player; Stopping server");
            }
            final String data = Utils.decode(request);
            game = Game.fromString(data);
            if (game.gameResult() != GameResult.NOT_FINISHED) {
                for (final Map.Entry<Mark, SocketAddress> player : players.entrySet()) {
                    response.setData(game.toString().getBytes());
                    response.setSocketAddress(player.getValue());
                    try {
                        datagramSocket.send(response);
                    } catch (final IOException e) {
                        throw new RuntimeException("Cannot send answer for player; Stopping server");
                    }
                }
                return;
            }
        }
    }

    private void serverCycle() {
        if (datagramSocket.isClosed()) {
            return;
        }
        registerPlayers();
        startGame();
        serverCycle();
    }

    public void start() {
        serverCycle();
    }

    public void close() {
        datagramSocket.close();
    }

    public static void main(final String[] args) {
        try {
            new Server(8888).start();
        } catch (final UnknownHostException | SocketException e) {
            System.err.println(e.getMessage());
        }
    }
}
