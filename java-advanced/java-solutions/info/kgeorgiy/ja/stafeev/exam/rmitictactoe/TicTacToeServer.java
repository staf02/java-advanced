package info.kgeorgiy.ja.stafeev.exam.rmitictactoe;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TicTacToeServer {
    private final static int DEFAULT_PORT = 8880;

    public static void main(final String[] args) throws InterruptedException, RemoteException {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        final Game ticTacToeGame = new TicTacToeGame();

        try {
            UnicastRemoteObject.exportObject(ticTacToeGame, port);
            Naming.rebind("//localhost/game", ticTacToeGame);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
            System.exit(1);
        }

        while (true) {
            synchronized (ticTacToeGame) {
                while (!ticTacToeGame.readyToStart()) {
                    ticTacToeGame.wait();
                }
            }
            System.out.println("Game started");
            synchronized (ticTacToeGame) {
                while (!ticTacToeGame.ended()) {
                    ticTacToeGame.wait(2);
                }
                System.out.println("Game ended");
            }
            ticTacToeGame.reset();
        }
    }
}
