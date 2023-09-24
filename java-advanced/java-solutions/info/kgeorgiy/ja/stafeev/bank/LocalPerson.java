package info.kgeorgiy.ja.stafeev.bank;

import java.rmi.RemoteException;
import java.util.Map;

public final class LocalPerson extends AbstractPerson {
    public LocalPerson(final String name, final String surName, final String passportId) {
        super(name, surName, passportId);
    }

    public LocalPerson(final Person other) throws RemoteException {
        super(other.getName(), other.getSurname(), other.getPassportId());
        for (final Map.Entry<String, Account> entry : other.getAccounts().entrySet()) {
            accounts.put(entry.getKey(), new LocalAccount(entry.getValue()));
        }
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        return accounts.computeIfAbsent(id, LocalAccount::new);
    }
}
