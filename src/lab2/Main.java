package lab2;

import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) {
        binSemSimulation();

        System.out.println("**********************");

        try {
            countSemSimulation(3, 6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("**********************");

        try {
            diningPhilosophersSimulation(5, 5, 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void diningPhilosophersSimulation(int forksCount, int philosophersCount, int solID) throws InterruptedException {
        DiningPhilosophers sim = new DiningPhilosophers(forksCount, philosophersCount);
        switch (solID) {
            case 1 -> sim.sol1();
            case 2 -> sim.sol2();
            case 3 -> sim.sol3();
            default -> System.out.println("There is not simulation for solID ==" + solID);
        }
    }

    /**
     * Sets up and runs the simulation for the counting semaphore using a parking lot analogy.
     * It creates a given number of 'car' threads that compete for access to the limited parking spaces.
     */
    private static void countSemSimulation(int parkingCapacity, int carsNum) throws IllegalArgumentException, InterruptedException {
        CountSemaphore sem = new CountSemaphore(parkingCapacity);
        Parking parking = new Parking(sem);

        Thread[] cars = new Thread[carsNum];

        for (int i = 0; i < cars.length; i++) {
            final String nazwa = "Samochód-" + i;

            cars[i] = new Thread(() -> {
                parking.enter(nazwa);

                try {
                    // Symulacja czasu, jaki samochód spędza na parkingu.
                    long czasParkowania = (long) (Math.random() * 6 + 2);
                    TimeUnit.SECONDS.sleep(czasParkowania);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                parking.leave(nazwa);
            });

            cars[i].start();
        }

        for (int i = 0; i < cars.length; i++) {
            try {
                cars[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void binSemSimulation() {
        Counter counter = new Counter();
        BinSemaphore sem = new BinSemaphore();

        Thread incT = new Thread(() -> {
            // million iterations
            for (int i = 0; i < 1000000; i++) {
                try {
                    sem.opusc();
                    counter.increment();
                    sem.podnies();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread decT = new Thread(() -> {
            // million iterations
            for (int i = 0; i < 1000000; i++) {
                try {
                    sem.opusc();
                    counter.decrement();
                    sem.podnies();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        incT.start();
        decT.start();

        try {
            incT.join();
            decT.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Bin simulation output counter: " + counter.counter);
    }
}
