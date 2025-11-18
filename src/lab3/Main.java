package lab3;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // sim1();
        sim2();
    }

    private static void sim1() throws InterruptedException {
        int clientsCount = 5;
        int printersCount = 3;

        MonitorDrukarek monitor = new MonitorDrukarek(printersCount);
        ArrayList<Thread> clients = new ArrayList<>(clientsCount);

        for (int i = 0; i < clientsCount; i++) {
            clients.add(new Thread(() -> {
                int task = 0;
                try {
                    while (true) {
                        task++;
                        String zadanie = "Zadanie " + task + " od " + Thread.currentThread().getName();
                        // 1. Rezerwacja
                        int printerId = monitor.zarezerwuj();

                        // 2. Drukowanie
                        System.out.println("==> " + Thread.currentThread().getName() + " DRUKUJE '" + zadanie + "' na drukarce " + printerId);
                        TimeUnit.SECONDS.sleep(2);
                        System.out.println("<== " + Thread.currentThread().getName() + " ZAKOŃCZYŁ drukowanie na " + printerId);

                        // 3. Zwolnienie drukarki
                        monitor.zwolnij(printerId);
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }, "Klient-" + i));
        }

        for (int i = 0; i < clientsCount; i++) {
            clients.get(i).start();
        }

    }

    private static void sim2() throws InterruptedException {
        int numPairs = 5;
        Waiter waiter = new Waiter(numPairs);
        ArrayList<Thread> persons = new ArrayList<>(numPairs * 2);

        for (int i = 0; i < numPairs; i++) {
            final int pairID = i;

            // 1. Pierwsza osoba
            Thread p1 = new Thread(() -> {
                try {
                    // 1. wlasne sprawy
                    System.out.println(Thread.currentThread().getName() + " załatwia własne sprawy...");
                    TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 4000) + 1000);  // 1 - 5 sec

                    // 2. chce_stolik
                    System.out.println(Thread.currentThread().getName() + " CHCE stolik.");
                    waiter.chce_stolik(pairID);

                    // 3. jedzenie
                    System.out.println(Thread.currentThread().getName() + " ZACZYNA jeść.");
                    TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 3000) + 1000); // 1 - 4 sec
                    System.out.println(Thread.currentThread().getName() + " KOŃCZY jeść.");

                    // 4. zwalniam
                    waiter.zwalniam(pairID);
                    System.out.println(Thread.currentThread().getName() + " ODSZEDŁ.");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Para-" + pairID + "-A");

            // Druga osoba
            Thread p2 = new Thread(() -> {
                try {
                    // 1. wlasne sprawy
                    System.out.println(Thread.currentThread().getName() + " załatwia własne sprawy...");
                    TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 4000) + 1000); // 1 - 5 sec

                    // 2. chce_stolik
                    System.out.println(Thread.currentThread().getName() + " CHCE stolik.");
                    waiter.chce_stolik(pairID);

                    // 3. jedzenie
                    System.out.println(Thread.currentThread().getName() + " ZACZYNA jeść.");
                    TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 3000) + 1000); // 1 - 4 sec
                    System.out.println(Thread.currentThread().getName() + " KOŃCZY jeść.");

                    // 4. zwalniam
                    waiter.zwalniam(pairID);
                    System.out.println(Thread.currentThread().getName() + " ODSZEDŁ.");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Para-" + pairID + "-B");

            persons.add(p1);
            persons.add(p2);
        }

        for (Thread p : persons) {
            p.start();
        }
    }
}
