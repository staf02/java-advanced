package info.kgeorgiy.ja.stafeev.exam.rmitictactoe;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class TicTacToeClient {

    public static void main(final String[] args) throws RemoteException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        final String playerId = scanner.next();
        final Game ticTacToeGame;
        try {
            ticTacToeGame = (Game) Naming.lookup("//localhost/game");
        } catch (final NotBoundException e) {
            System.out.println("Game is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Game is invalid");
            return;
        }
        ticTacToeGame.join(playerId);
        if (ticTacToeGame.readyToStart()) {
            System.out.println("Game started");
        }
        while (true) {
            synchronized (ticTacToeGame) {
                if (!ticTacToeGame.readyToStart()) {
                    while (!ticTacToeGame.readyToStart()) {
                        System.out.println("waiting for other player");
                        ticTacToeGame.wait(1000);
                    }
                }
            }
            synchronized (ticTacToeGame) {
                while (!ticTacToeGame.readyToMakeTurn(playerId) && !ticTacToeGame.ended()) {
                    ticTacToeGame.wait(500);
                }
                final String result = ticTacToeGame.getResult();
                if (result != null) {
                    System.out.println(result);
                    System.out.println("Restarting game");
                    ticTacToeGame.endGame();
                    continue;
                }
                System.out.println(playerId + " ready to make turn");
            }
            while (true) {
                try {
                    System.out.println(ticTacToeGame.renderGame());
                    int x = scanner.nextInt(), y = scanner.nextInt();
                    ticTacToeGame.makeTurn(x, y, playerId);
                    System.out.println("After your turn " + playerId);
                    System.out.println(ticTacToeGame.renderGame());
                    break;
                } catch (final TicTacToeException e) {
                    System.err.println(e.getMessage());
                }
            }

            final String result = ticTacToeGame.getResult();
            if (result != null) {
                System.out.println(result);
                System.out.println("Restarting game");
                ticTacToeGame.endGame();
            }
        }
    }
}
