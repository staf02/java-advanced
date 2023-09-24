package info.kgeorgiy.ja.stafeev.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractPerson implements Person, Serializable {

    private final String name;
    private final String surName;
    private final String passportId;
    protected final Map<String, Account> accounts;

    public AbstractPerson(final String name, final String surName, final String passportId) {
        this.name = name;
        this.surName = surName;
        this.passportId = passportId;

        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() throws RemoteException {
        return surName;
    }

    @Override
    public String getPassportId() throws RemoteException {
        return passportId;
    }

    @Override
    public Map<String, Account> getAccounts() throws RemoteException {
        return Collections.unmodifiableMap(accounts);
    }

    @Override
    public Account getAccountById(final String id) throws RemoteException {
        return accounts.get(id);
    }
}
