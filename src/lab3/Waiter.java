package lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Waiter {
    private final Lock lock = new ReentrantLock();
    private boolean isTableBusy = false;
    // liczba osob z pary przy stole (0, 1, lub 2)
    private final int[] pairState;
    private final List<Condition> partnerWaitingConds;
    private final Condition tableFreeCond = lock.newCondition();

    public Waiter(int numPairs) {
        this.pairState = new int[numPairs]; // Default 0
        this.partnerWaitingConds = new ArrayList<>(numPairs);

        for (int i = 0; i < numPairs; i++) {
            partnerWaitingConds.add(lock.newCondition());
        }
    }

    public void chce_stolik(int pairID) throws InterruptedException {
        lock.lock();
        try {
            if (pairState[pairID] == 0) { // the first person
                pairState[pairID] = 1;

                // Waiting for 2nd person to arrive and book the table
                System.out.println(Thread.currentThread().getName() + " (Pair " + pairID + "): Jestem pierwszy czekam na partnera.");
                partnerWaitingConds.get(pairID).await();

                System.out.println(Thread.currentThread().getName() + " (Pair " + pairID + "): Partner przyszedł zaczynamy jeść.");
            } else { // the second person
                System.out.println(Thread.currentThread().getName() + " (Pair " + pairID + "): Jestem drugi, patrzę na stolik");

                while (isTableBusy) {
                    System.out.println(Thread.currentThread().getName() + " (Pair " + pairID + "): Stolik zajęty czekam.");
                    tableFreeCond.await();
                }

                // booking the table
                isTableBusy = true;

                // set pair state to eating
                pairState[pairID] = 2;

                // wake up partner
                System.out.println(Thread.currentThread().getName() + " (Pair " + pairID + "): Zająłem stolik. Budzę partnera.");
                partnerWaitingConds.get(pairID).signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void zwalniam(int pairID) {
        lock.lock();
        try {
            // Person leaves the table - reduce number of ppl at the table
            pairState[pairID]--;
            System.out.println(Thread.currentThread().getName() + " (Pair " + pairID + "): Odchodzę od stolika.");

            if (pairState[pairID] == 0) {
                // last person leaving is also freeing the table
                isTableBusy = false;
                System.out.println(Thread.currentThread().getName() + " (Pair " + pairID + "): Obie osoby z pary odeszly. Stolik jest wolny.");

                tableFreeCond.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}