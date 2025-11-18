package lab4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProdConsStarving {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition producerCond = lock.newCondition();
    private final Condition consumerCond = lock.newCondition();

    private final int maxCapacity;
    private int count = 0;
    private final FileWriter log;

    public ProdConsStarving(int M, FileWriter log) {
        this.maxCapacity = 2 * M;
        this.log = log;
    }

    public void produce(int amount) throws InterruptedException, IOException {
        lock.lock();
        try {
            long start = System.nanoTime();

            while (count + amount > maxCapacity) {
                if (!producerCond.await(100, TimeUnit.MILLISECONDS)) {
                    producerCond.signal();
                    return;
                }
            }

            count += amount;
            log.append("Producer," + amount + "," + (System.nanoTime() - start) + "\n");
            consumerCond.signal();

        } finally {
            lock.unlock();
        }
    }

    public void consume(int amount) throws InterruptedException, IOException {
        lock.lock();
        try {
            long start = System.nanoTime();

            while (count < amount) {
                if (!consumerCond.await(100, TimeUnit.MILLISECONDS)) {
                    consumerCond.signal();
                    return;
                }
            }

            count -= amount;
            log.append("Consumer," + amount + "," + (System.nanoTime() - start) + "\n");
            producerCond.signal();

        } finally {
            lock.unlock();
        }
    }
}