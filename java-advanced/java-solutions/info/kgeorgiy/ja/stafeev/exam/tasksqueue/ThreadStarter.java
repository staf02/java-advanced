package info.kgeorgiy.ja.stafeev.exam.tasksqueue;

import java.util.Random;

public class ThreadStarter {
    public static void main(final String[] args) throws InterruptedException {
        ConcurrentQueue<Integer> queue = new ConcurrentQueue<>();
        Thread producer = new Thread(() -> {
            final Random random = new Random();
            while (true) {
                try {
                    int val = random.nextInt(100000);
                    queue.add(val);
                    System.out.println(val + " added");
                    long time = random.nextInt(2);
                    Thread.sleep(time);
                } catch (final InterruptedException ignored) {

                }
            }
        });
        Thread getter = new Thread(() -> {
            final Random random = new Random();
            while (true) {
                try {
                    while (true) {
                        Thread.sleep(120);
                        System.out.println(queue.poll() + " polled");
                    }
                } catch (final InterruptedException ignored) {

                }
            }
        });
        producer.start();
        getter.start();
        producer.join();
        getter.join();
    }
}
