package info.kgeorgiy.ja.stafeev.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public final class RemotePerson extends AbstractPerson {

    final int port;
    public RemotePerson(final String name, final String surName, final String passportId, final int port) {
        super(name, surName, passportId);
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
        }
        return accounts.get(id);
    }
}
