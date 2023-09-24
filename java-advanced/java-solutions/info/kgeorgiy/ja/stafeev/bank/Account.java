package info.kgeorgiy.ja.stafeev.bank;

import java.io.Serializable;
import java.rmi.*;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money in the account. */
    long getAmount() throws RemoteException;

    /** Sets amount of money in the account. */
    void setAmount(long amount) throws RemoteException;

    void increaseAmount(long amount) throws RemoteException;
}
