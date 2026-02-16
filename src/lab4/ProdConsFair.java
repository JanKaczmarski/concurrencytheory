package lab4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProdConsFair {

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition producersQueue;
    private final Condition producersTurn;
    private final Condition consumersQueue;
    private final Condition consumersTurn;

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
        lock.lock();
        try {
            long start = System.nanoTime();

            if (lock.hasWaiters(producersQueue)) {
                if (!producersTurn.await(100, TimeUnit.MILLISECONDS)) {
                    producersTurn.signal();
                    return; // Timeout
                }
            }

            // Wait for enough space
            while (count + amount > maxCapacity) {
                if (!producersQueue.await(100, TimeUnit.MILLISECONDS)) {
                    producersTurn.signal();
                    return; // Timeout
                }
            }

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
        lock.lock();
        try {
            long start = System.nanoTime();

            // Fair ordering: wait for our turn if others are waiting
            if (lock.hasWaiters(consumersQueue)) {
                if (!consumersTurn.await(100, TimeUnit.MILLISECONDS)) {
                    consumersTurn.signal();
                    return; // Timeout
                }
            }

            // Wait for enough items
            while (count < amount) {
                if (!consumersQueue.await(100, TimeUnit.MILLISECONDS)) {
                    consumersTurn.signal();
                    return; // Timeout
                }
            }

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