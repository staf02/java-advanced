package info.kgeorgiy.ja.stafeev.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public class AbstractAccount implements Account, Serializable {

    private final String id;
    private long amount;

    public AbstractAccount(String id) {
        this.id = id;
        this.amount = 0;
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public synchronized long getAmount() throws RemoteException {
        return amount;
    }

    @Override
    public synchronized void setAmount(final long amount) throws RemoteException {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount of money is negative");
        }
        this.amount = amount;
    }

    @Override
    public synchronized void increaseAmount(final long i) throws RemoteException {
        if (amount + i < 0) {
            throw new IllegalArgumentException("Amount of money is negative");
        }
        amount += i;
    }
}
