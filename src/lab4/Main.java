package lab4;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("=== Testy Producer-Consumer ===\n");

        System.out.println("Test 1: M=1000, 10P+10C");
        runTest(100000, 1000, "log_starving.csv", "log_fair.csv");


        // Test configuration 3: M=100000, 1000 producers, 1000 consumers
        // System.out.println("\nTest 3: M=100000, 1000P+1000C");
        // runTest(100000, 1000, "log_starving_100000.csv", "log_fair_100000.csv");

        System.out.println("\n=== Wszystkie testy zakończone ===");
    }

    private static void runTest(int M, int threadCount, String starvingLog, String fairLog)
            throws InterruptedException, IOException {

        // Test 1: Naive approach (with starvation)
        System.out.println("  Wariant naiwny...");
        FileWriter log1 = new FileWriter(starvingLog);
        log1.append("Type,Amount,WaitTimeNanos\n");

        ProdConsStarving buffer1 = new ProdConsStarving(M, log1);
        Thread[] threads1 = new Thread[threadCount * 2];

        // Create producer threads
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            threads1[i] = new Thread(() -> {
                try {
                    int amount = (int) (Math.random() * M) + 1;
                    buffer1.produce(amount);
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Producer-" + id);
        }

        // Create consumer threads
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            threads1[i + threadCount] = new Thread(() -> {
                try {
                    int amount = (int) (Math.random() * M) + 1;
                    buffer1.consume(amount);
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-" + id);
        }

        long start1 = System.currentTimeMillis();
        for (Thread t : threads1) t.start();
        for (Thread t : threads1) t.join();
        long end1 = System.currentTimeMillis();

        log1.close();
        System.out.println("    Zakończono w " + (end1 - start1) + " ms");

        // Test 2: Fair approach (without starvation)
        System.out.println("  Wariant sprawiedliwy...");
        FileWriter log2 = new FileWriter(fairLog);
        log2.append("Type,Amount,WaitTimeNanos\n");

        ProdConsFair buffer2 = new ProdConsFair(M, log2);
        Thread[] threads2 = new Thread[threadCount * 2];

        // Create producer threads
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            threads2[i] = new Thread(() -> {
                try {
                    int amount = (int) (Math.random() * M) + 1;
                    buffer2.produce(amount);
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Producer-" + id);
        }

        // Create consumer threads
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            threads2[i + threadCount] = new Thread(() -> {
                try {
                    int amount = (int) (Math.random() * M) + 1;
                    buffer2.consume(amount);
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-" + id);
        }

        long start2 = System.currentTimeMillis();
        for (Thread t : threads2) t.start();
        for (Thread t : threads2) t.join();
        long end2 = System.currentTimeMillis();

        log2.close();
        System.out.println("    Zakończono w " + (end2 - start2) + " ms");
    }
}