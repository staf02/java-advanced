package info.kgeorgiy.ja.stafeev.bank;

import info.kgeorgiy.ja.stafeev.exam.filemanager.FileManagerException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import static org.junit.Assert.*;

@TestMethodOrder(MethodOrderer.MethodName.class)

public class BankTest {
    @BeforeClass
    public static void runBank() {
        final Bank bank = new RemoteBank(8888);
        try {
            UnicastRemoteObject.exportObject(bank, 8888);
            Naming.rebind("//localhost/bank", bank);
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Bank getBank() throws RemoteException {
        try {
            return  (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1() throws RemoteException {
        Bank bank = getBank();
        bank.createPerson("Grigorii", "Stafeev", "1234");
        assertNotNull(bank.getPerson("1234"));
    }

    @Test
    public void test2() throws RemoteException {
        Bank bank = getBank();
        Person grigorii = bank.getPerson("1234");
        Account account = grigorii.createAccount("5678");
        account.setAmount(1000);
        Account fullAccount = bank.getAccount("1234:5678");
        assertNotNull(fullAccount);
        assertEquals(fullAccount.getAmount(), account.getAmount());
    }

    @Test
    public void test3() throws RemoteException {
        Bank bank = getBank();
        Person localGrigorii = bank.getLocalPerson("1234");
        localGrigorii.createAccount("12345");
        Person grigorii = bank.getPerson("1234");
        assertNull(grigorii.getAccountById("12345"));
        assertNull(bank.getAccount("1234:12345"));
    }

    @Test
    public void test4() throws RemoteException, InterruptedException {
        Bank bank = getBank();
        Account account = bank.getAccount("1234:5678");
        final Runnable runnable = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                    account.increaseAmount(1000);
                } catch (final RemoteException e) {
                    throw new RuntimeException();
                }
            }
        };
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertEquals( 21000, account.getAmount());
    }
}
