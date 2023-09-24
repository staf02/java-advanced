package info.kgeorgiy.ja.stafeev.exam.stringmanager;

import java.io.IOException;
import java.nio.file.Path;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

public interface Core extends Remote {
    List<String> getAllStrings() throws RemoteException;
    void addString(String str) throws RemoteException;
    void removeString(String str) throws RemoteException;
    void getInfo(Collection<? extends String> strings) throws RemoteException;
    void writeToFile(final Path outputPath) throws RemoteException, IOException;
}
