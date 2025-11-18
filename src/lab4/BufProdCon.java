package lab4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.*;

public class BufProdCon {
    private final List<Integer> buf;
    private final Lock lock = new ReentrantLock();
    private final Condition[] stages;
    private final int numStages;

    public BufProdCon(int bufSize, int numStages) {
        this.buf = new ArrayList<>(bufSize);
        this.numStages = numStages;
        for (int i = 0; i < bufSize; i++) buf.add(0);

        stages = new Condition[numStages];
        for (int i = 0; i < numStages; i++) {
            stages[i] = lock.newCondition();
        }
    }

    public void start() {
        // Producent
        new Thread(() -> processStage(0), "Producer").start();

        // Przetwarzacze
        for (int i = 1; i < numStages - 1; i++) {
            int stage = i;
            new Thread(() -> processStage(stage), "Processor-" + stage).start();
        }

        // Konsument
        new Thread(() -> processStage(numStages - 1), "Consumer").start();
    }

    private void processStage(int stageId) {
        try {
            while (true) {
                for (int i = 0; i < buf.size(); i++) {
                    lock.lock();
                    try {
                        // PRODUCENT
                        if (stageId == 0) {
                            while (buf.get(i) != 0)
                                stages[stageId].await();
                            buf.set(i, 1);
                            System.out.printf("[%s] Produced -> index %d%n", Thread.currentThread().getName(), i);
                        }

                        // PRZETWARZACZE
                        else if (stageId < numStages - 1) {
                            while (buf.get(i) != stageId)
                                stages[stageId].await();
                            buf.set(i, stageId + 1);
                            System.out.printf("[%s] Processed index %d -> %d%n",
                                    Thread.currentThread().getName(), i, stageId + 1);
                        }

                        // KONSUMENT
                        else {
                            while (buf.get(i) == 0)
                                stages[stageId].await();
                            System.out.printf("[%s] Consumed index %d (val=%d)%n",
                                    Thread.currentThread().getName(), i, buf.get(i));
                            buf.set(i, 0);
                        }

                        stages[(stageId + 1)%numStages].signal();

                        Thread.sleep(100); // różne prędkości
                    } finally {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
