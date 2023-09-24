package info.kgeorgiy.ja.stafeev.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote, Serializable {
    String getName() throws RemoteException;
    String getSurname() throws RemoteException;
    String getPassportId() throws RemoteException;

    Map<String, Account> getAccounts() throws RemoteException;
    Account getAccountById(String id) throws RemoteException;
    Account createAccount(String id) throws RemoteException;
}
