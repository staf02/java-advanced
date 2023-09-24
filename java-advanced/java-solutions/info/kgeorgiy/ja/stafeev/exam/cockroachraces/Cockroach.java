package info.kgeorgiy.ja.stafeev.exam.cockroachraces;

import java.util.Arrays;
import java.util.Queue;

public class Cockroach implements Runnable {

    private final int id;
    private boolean finished;
    private int[] arr;
    public Cockroach(int id) {
        this.id = id;
        this.finished = false;
    }

    public void setArr(final int[] arr) {
        this.arr = arr;
    }

    public int getId() {
        return id;
    }

    public boolean isFinished() {
        return finished;
    }

    public void reset() {
        finished = false;
    }

    public synchronized void run() {
        Arrays.sort(arr);
        finished = true;
        notify();
    }
}
