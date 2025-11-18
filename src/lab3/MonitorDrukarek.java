package lab3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class MonitorDrukarek {
    private final Lock lock = new ReentrantLock();
    private final Condition drukarkaDostepna = lock.newCondition();
    private final Queue<Integer> wolneDrukarki = new LinkedList<>();

    public MonitorDrukarek(int printersAmount) {
        for (int i = 0; i < printersAmount; i++) {
            wolneDrukarki.add(i);
        }
    }

    public int zarezerwuj() throws InterruptedException {
        lock.lock();
        try {
            while (wolneDrukarki.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + " CZEKA na drukarkę...");
                drukarkaDostepna.await();
            }

            int idDrukarki = wolneDrukarki.remove();
            System.out.println(Thread.currentThread().getName() + " zarezerwował drukarkę " + idDrukarki);
            return idDrukarki;

        } finally {
            lock.unlock();
        }
    }

    public void zwolnij(int nrDrukarki) {
        lock.lock();
        try {
            wolneDrukarki.add(nrDrukarki);
            System.out.println(Thread.currentThread().getName() + " zwolnił drukarkę " + nrDrukarki);

            drukarkaDostepna.signal();

        } finally {
            lock.unlock();
        }
    }
}