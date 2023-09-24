package info.kgeorgiy.ja.stafeev.exam.rmitictactoe;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class TicTacToeGame implements Game {

    final Mark[][] board;
    final Map<String, Mark> markToPlayer;
    final String[] players;
    int freePlayers;
    int turn;
    int free;
    boolean started;

    String gameResult;
    TicTacToeGame() {
        board = new Mark[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Mark.Empty;
            }
        }

        markToPlayer = new HashMap<>();
        players = new String[2];
        turn = 0;
        free = 0;
        freePlayers = 0;
        started = false;
        gameResult = null;
    }

    @Override
    public String renderGame() throws RemoteException {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                stringBuilder.append(board[i][j]);
            }
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    public synchronized void makeTurn(int x, int y, final String playerId) throws TicTacToeException {
        x--;
        y--;
        if (!(x >= 0 && x < 3 && y >= 0 && y < 3)) {
            throw new TicTacToeException("bad x y");
        } else if (board[x][y] != Mark.Empty) {
            throw new TicTacToeException("not free");
        } else if (!playerId.equals(players[turn])) {
            throw new TicTacToeException("not you turn");
        } else if (markToPlayer.size() != 2) {
            throw new TicTacToeException("not started");
        } else if (!started) {
            throw new TicTacToeException("ended");
        }
        board[x][y] = markToPlayer.get(playerId);
        free++;
        if (checkWin(markToPlayer.get(playerId))) {
            gameResult = playerId + " - winner";
            started = false;
        } else if (free == 9) {
            gameResult = "draw";
            started = false;
        } else {
            turn++;
            turn %= 2;
        }
        this.notifyAll();
    }

    public synchronized boolean ended() {
        return !started;
    }

    public synchronized void reset() throws InterruptedException {
        while (freePlayers > 0) {
            this.wait(500);
        }
        gameResult = null;
        free = 0;
        turn = 0;
        started = true;
        freePlayers = markToPlayer.size();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Mark.Empty;
            }
        }

        this.notifyAll();
    }

    @Override
    public synchronized void endGame() throws RemoteException {
        freePlayers--;
        this.notifyAll();
    }

    @Override
    public synchronized String getResult() throws RemoteException {
        return gameResult;
    }

    public synchronized void join(final String playerId) {
        if (markToPlayer.size() == 0) {
            markToPlayer.put(playerId, Mark.X);
            freePlayers++;
            players[0] = playerId;
            System.out.println("Player " + playerId + " joined as first player");
        } else if (markToPlayer.size() == 1) {
            markToPlayer.put(playerId, Mark.O);
            freePlayers++;
            players[1] = playerId;
            System.out.println("Player " + playerId + " joined as second player");
            started = true;
            this.notifyAll();
        } else {
            System.err.println("Too many players");
        }
    }

    public synchronized boolean readyToStart() {
        return started;
    }

    public boolean checkWin(final Mark mark) {
        return board[0][0] == board[0][1] && board[0][1] == board[0][2] && board[0][2] == mark ||
                board[1][0] == board[1][1] && board[1][1] == board[1][2] && board[1][2] == mark ||
                board[2][0] == board[2][1] && board[2][1] == board[2][2] && board[2][2] == mark ||
                board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[2][2] == mark ||
                board[2][0] == board[1][1] && board[1][1] == board[0][2] && board[0][2] == mark ||
                board[0][0] == board[1][0] && board[1][0] == board[2][0] && board[2][0] == mark ||
                board[0][1] == board[1][1] && board[1][1] == board[2][1] && board[2][1] == mark ||
                board[0][2] == board[1][2] && board[1][2] == board[2][2] && board[2][2] == mark;
    }

    public synchronized boolean readyToMakeTurn(final String playerId) {
        return players[turn].equals(playerId);
    }
}
