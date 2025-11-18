package lab4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProdConsFair {

    private final ReentrantLock lock = new ReentrantLock();

    // Fair queuing with 4 conditions
    private final Condition producersQueue;      // Producers wait here for space
    private final Condition producersTurn;       // Producers wait here for their turn (fairness)
    private final Condition consumersQueue;      // Consumers wait here for items
    private final Condition consumersTurn;       // Consumers wait here for their turn (fairness)

    private final int maxCapacity;
    private int count = 0;

    private final FileWriter log;

    public ProdConsFair(int M, FileWriter log) {
        this.maxCapacity = 2 * M;
        this.log = log;

        producersQueue = lock.newCondition();
        producersTurn = lock.newCondition();
        consumersQueue = lock.newCondition();
        consumersTurn = lock.newCondition();
    }

    public void produce(int amount) throws InterruptedException, IOException {
        if (amount > maxCapacity) {
            return; // Cannot produce more than buffer capacity
        }

        lock.lock();
        try {
            long start = System.nanoTime();

            // Fair ordering: wait for our turn if others are waiting
            if (lock.hasWaiters(producersQueue)) {
                if (!producersTurn.await(100, TimeUnit.MILLISECONDS)) {
                    producersTurn.signal();
                    return; // Timeout - exit gracefully
                }
            }

            // Wait for enough space
            while (count + amount > maxCapacity) {
                if (!producersQueue.await(100, TimeUnit.MILLISECONDS)) {
                    producersTurn.signal();
                    return; // Timeout - exit gracefully
                }
            }

            // Produce
            count += amount;

            synchronized (log) {
                log.write("Producer," + amount + "," + (System.nanoTime() - start) + "\n");
            }

            // Signal consumers and next producer
            consumersQueue.signal();
            producersTurn.signal();

        } finally {
            lock.unlock();
        }
    }

    public void consume(int amount) throws InterruptedException, IOException {
        if (amount > maxCapacity) {
            return; // Cannot consume more than buffer capacity
        }

        lock.lock();
        try {
            long start = System.nanoTime();

            // Fair ordering: wait for our turn if others are waiting
            if (lock.hasWaiters(consumersQueue)) {
                if (!consumersTurn.await(100, TimeUnit.MILLISECONDS)) {
                    consumersTurn.signal();
                    return; // Timeout - exit gracefully
                }
            }

            // Wait for enough items
            while (count < amount) {
                if (!consumersQueue.await(100, TimeUnit.MILLISECONDS)) {
                    consumersTurn.signal();
                    return; // Timeout - exit gracefully
                }
            }

            // Consume
            count -= amount;

            synchronized (log) {
                log.write("Consumer," + amount + "," + (System.nanoTime() - start) + "\n");
            }

            // Signal producers and next consumer
            producersQueue.signal();
            consumersTurn.signal();

        } finally {
            lock.unlock();
        }
    }
}