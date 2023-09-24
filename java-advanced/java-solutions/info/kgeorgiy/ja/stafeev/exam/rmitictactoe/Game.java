package info.kgeorgiy.ja.stafeev.exam.rmitictactoe;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Game extends Remote {
    void makeTurn(int x, int y, String playerId) throws TicTacToeException, RemoteException;

    boolean ended() throws RemoteException;

    void join(String playerId) throws RemoteException;

    boolean readyToStart() throws RemoteException;

    boolean readyToMakeTurn(String playerId) throws RemoteException;

    String renderGame() throws RemoteException;

    void reset() throws RemoteException, InterruptedException;

    void endGame() throws RemoteException;

    String getResult() throws RemoteException;
}
