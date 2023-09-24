package info.kgeorgiy.ja.stafeev.exam.tracingproxy;

public class TestClass implements Printer {
    public void print() {
        System.out.println("TestClass");
        throw new RuntimeException();
    }
}
