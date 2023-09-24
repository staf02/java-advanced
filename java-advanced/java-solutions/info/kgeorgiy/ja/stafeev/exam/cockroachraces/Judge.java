package info.kgeorgiy.ja.stafeev.exam.cockroachraces;

import java.util.*;
import java.util.stream.IntStream;

public class Judge {

    private static final int MAX_SIZE = 100_000;

    private static int[] getRandomArray() {
        final int[] res = new int[MAX_SIZE];
        final Random random = new Random();
        IntStream.range(0, MAX_SIZE).forEach(i -> res[i] = random.nextInt(Integer.MAX_VALUE));
        return res;
    }

    private final List<Cockroach> cockroaches;
    private final Queue<Integer> winners;
    private final int[] commonResults;
    private final int n;
    private final int m;

    private Judge(int n, int m) {
        this.n = n;
        this.m = m;

        this.commonResults = new int[n];
        this.cockroaches = new ArrayList<>();
        this.winners = new ArrayDeque<>();

        IntStream.range(1, n + 1).forEach(i -> cockroaches.add(new Cockroach(i)));
    }

    private Runnable getRunnable(final int i) {
        return () -> {
            try {
                cockroaches.get(i).run();
                synchronized (cockroaches.get(i)) {
                    while (!cockroaches.get(i).isFinished() && !Thread.currentThread().isInterrupted()) {
                        cockroaches.get(i).wait();
                    }
                }
                synchronized (winners) {
                    winners.add(cockroaches.get(i).getId());
                    if (winners.size() == n) {
                        winners.notify();
                    }
                }
            } catch (final InterruptedException ignored) {

            } finally {
                Thread.currentThread().interrupt();
            }
        };
    }

    private void phase(int phaseNum) throws InterruptedException {
        winners.clear();
        cockroaches.forEach(Cockroach::reset);

        System.out.println("PHASE # " + phaseNum);
        final List<Thread> workers = new ArrayList<>();

        IntStream.range(0, n).forEach(i -> workers.add(new Thread(getRunnable(i))));

        for (int i = 0; i < n; i++) {
            cockroaches.get(i).setArr(getRandomArray());
        }

        workers.forEach(Thread::start);

        int[] currentRound = new int[n];

        synchronized (winners) {
            while (winners.size() != n) {
                winners.wait();
            }
            int points = n;
            while (!winners.isEmpty()) {
                int cockroachNum = winners.poll();
                currentRound[cockroachNum - 1] = points--;
            }
        }

        System.out.println("Current round results");
        for (
                int i = 0;
                i < n; i++) {
            System.out.println("# " + (i + 1) + " " + currentRound[i]);
        }

        commonStatistic(currentRound);
        System.out.println("===================");
    }

    void commonStatistic(final int[] newRound) {
        System.out.println("Common statistics");
        for (int i = 0; i < n; i++) {
            commonResults[i] += newRound[i];
            System.out.println("# " + (i + 1) + " " + commonResults[i]);
        }
    }

    void play() {
        for (int i = 1; i <= m; i++) {
            try {
                phase(i);
            } catch (final InterruptedException e) {
                System.err.println("Game was interrupted");
                break;
            }
        }
    }

    public static void main(final String[] args) throws InterruptedException {
        final Scanner in = new Scanner(System.in);
        new Judge(in.nextInt(), in.nextInt()).play();
    }
}
