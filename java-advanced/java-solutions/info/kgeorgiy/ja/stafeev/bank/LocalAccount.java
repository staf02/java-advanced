package info.kgeorgiy.ja.stafeev.bank;

import java.rmi.RemoteException;

public final class LocalAccount extends AbstractAccount {
    public LocalAccount(final String id) {
        super(id);
    }

    public LocalAccount(final Account other) throws RemoteException {
        super(other.getId());
        setAmount(other.getAmount());
    }
}
