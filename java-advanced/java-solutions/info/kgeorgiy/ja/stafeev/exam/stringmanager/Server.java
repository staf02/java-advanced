package info.kgeorgiy.ja.stafeev.exam.stringmanager;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

public class Server {
    private final static int DEFAULT_PORT = 8888;

    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        final Core stringCore = new RemoteCore();
        try {
            UnicastRemoteObject.exportObject(stringCore, port);
            Naming.rebind("//localhost/stringcore", stringCore);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
