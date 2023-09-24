package info.kgeorgiy.ja.stafeev.exam.stringmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class RemoteCore implements Core {

    private final Set<String> data;
    public RemoteCore() {
        this.data = new HashSet<>();
    }

    public RemoteCore(final Path inputPath) throws IOException {
        this.data = new HashSet<>();

        BufferedReader bufferedReader = Files.newBufferedReader(inputPath);
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            data.add(str);
        }
    }
    @Override
    public List<String> getAllStrings() throws RemoteException {
        return data.stream().toList();
    }

    @Override
    public synchronized void addString(final String str) throws RemoteException {
        if (data.contains(str)) {
            System.out.println(str + " is already loaded");
        }
        data.add(str);
    }

    @Override
    public synchronized void removeString(final String str) throws RemoteException {
        if (!data.remove(str)) {
            System.out.println(str + " didn't loaded");
        }
    }

    @Override
    public void getInfo(final Collection<? extends String> strings) throws RemoteException {
        for (final String string : strings) {
            if (data.contains(string)) {
                System.out.println(string + " contains in core");
            }
        }
    }

    @Override
    public void writeToFile(Path outputPath) throws RemoteException, IOException {
        final BufferedWriter bufferedWriter = Files.newBufferedWriter(outputPath);
        for (final String s : data) {
            bufferedWriter.write(s);
        }
    }
}
