package info.kgeorgiy.ja.stafeev.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class BankClient {
    /** Utility class. */
    private BankClient() {}

    public static void main(final String[] args) throws RemoteException {
        if (args == null || args.length != 5) {
            System.err.println();
            return;
        }
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        final String name = args[0];
        final String surname = args[1];
        final String passport = args[2];
        final String accountId = args[3];
        final long amount;
        try {
            amount = Long.parseLong(args[4]);
        } catch (final NumberFormatException e) {
            System.err.println(args[4] + " is not a number");
            return;
        }

        Person person = bank.getPerson(passport);
        if (person == null) {
            System.out.println("Creating person");
            person = bank.createPerson(name, surname, passport);
        } else {
            System.out.println("Person already exists");
        }

        Account account = bank.getAccount(passport + ":" + accountId);

        if (account == null) {
            System.out.println("Creating account");
            account = bank.createAccount(passport + ":" + accountId);
        } else {
            System.out.println("Account already exists");
        }

        account.setAmount(account.getAmount() + amount);

        System.out.println(account.getAmount());
    }
}
