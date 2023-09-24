package info.kgeorgiy.ja.stafeev.exam.stringmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class Client {
    public static void main(final String... args) throws RemoteException {
        final Core core;
        try {
            core = (Core) Naming.lookup("//localhost/stringcore");
        } catch (final NotBoundException e) {
            System.out.println("String core is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("String core is invalid");
            return;
        }

        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String cmd;
            while ((cmd = bufferedReader.readLine()) != null) {
                final String[] tokens = cmd.split(" ", 2);
                switch (tokens[0]) {
                    case "add" -> {
                        core.addString(tokens[1]);
                    } case "remove" -> {
                        core.removeString(tokens[1]);
                    } case "printAll" -> {
                        final List<String> allStrings = core.getAllStrings();
                        for (final String s : allStrings) {
                            System.out.println(s);
                        }
                    } default -> {
                        System.err.println("Error");
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        core.addString("Abba");
        core.removeString("Hello");

        for (String s : core.getAllStrings()) {
            System.out.println(s);
        }
    }
}
