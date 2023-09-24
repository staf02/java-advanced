package info.kgeorgiy.ja.stafeev.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final Map<String, Person> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        try {
            final String[] tokens = id.split(":");
            return persons.get(tokens[0]).createAccount(tokens[1]);
        }
        catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Account getAccount(final String id) throws RemoteException {
        try {
            final String[] tokens = id.split(":");
            return persons.get(tokens[0]).getAccountById(tokens[1]);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Account getLocalAccount(final String id) throws RemoteException {
        return new LocalAccount(getAccount(id));
    }

    @Override
    public Person createPerson(final String name, final String surname, final String passportId) throws RemoteException {
        final Person person = new RemotePerson(name, surname, passportId, port);
        if (persons.putIfAbsent(passportId, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return persons.get(passportId);
        }
    }

    @Override
    public Person getPerson(final String passportId) throws RemoteException {
        return persons.get(passportId);
    }

    @Override
    public Person getLocalPerson(final String passportId) throws RemoteException {
        return new LocalPerson(getPerson(passportId));
    }

}
