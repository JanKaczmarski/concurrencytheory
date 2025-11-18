package lab2;

import java.util.ArrayList;
import java.util.List;

public class DiningPhilosophers {
    private final List<BinSemaphore> forks;
    private final List<Thread> philosophers;

    public DiningPhilosophers(int numberOfForks, int numberOfPhilosophers) {
        forks = new ArrayList<>(numberOfForks);
        philosophers = new ArrayList<>(numberOfPhilosophers);
        for (int i = 0; i < numberOfForks; i++) {
            forks.add(new BinSemaphore());
        }
        for (int i = 0; i < numberOfPhilosophers; i++) {
            philosophers.add(new Thread(() -> {}));
        }
    }

    public void sol1() throws InterruptedException {
        for (int i = 0; i < philosophers.size(); i++) {
            final int id = i;
            philosophers.set(i, new Thread(() -> {
                int right = id;
                int left = (id + 1) % forks.size();
                while (true) {

                    // 1. THINK
                    try {
                        System.out.println(Thread.currentThread().getName() + ": Myśli...");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 2. ACQUIRE FORKS
                    try {
                        System.out.println(Thread.currentThread().getName() + ": Chce podniesc prawy widelec (fork " + right + ")");
                        forks.get(right).opusc();
                        System.out.println(Thread.currentThread().getName() + ": Podniósł prawy widelec (fork " + right + ")");

//                        try {
//                            System.out.println(Thread.currentThread().getName() + ": ...pauza przed wzięciem lewego widelca...");
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                        System.out.println(Thread.currentThread().getName() + ": Chce podniesc lewy widelec (fork " + left + ")");
                        forks.get(left).opusc();
                        System.out.println(Thread.currentThread().getName() + ": Podniósł na lewy widelec (fork " + left + ")");

                        // 3. EAT
                        try {
                            System.out.println(Thread.currentThread().getName() + ": ZACZYNA JEŚĆ");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 4. Release
                        System.out.println(Thread.currentThread().getName() + ": Odklada oba widelce");
                        forks.get(right).podnies();
                        forks.get(left).podnies();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Philosopher " + id));
        }

        for (Thread thread : philosophers) {
            thread.start();
        }

        for (Thread thread : philosophers) {
            thread.join();
        }
    }

    public void sol2() throws InterruptedException {
        for (int i = 0; i < philosophers.size(); i++) {
            final int id = i;
            philosophers.set(i, new Thread(() -> {
                int right = id;
                int left = (id + 1) % forks.size();
                while (true) {

                    // 1. THINK
                    try {
                        System.out.println(Thread.currentThread().getName() + ": Myśli...");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 2. ACQUIRE FORKS
                    try {
                        if (id % 2 == 0) {
                            System.out.println(Thread.currentThread().getName() + ": Chce podniesc prawy widelec (fork " + right + ")");
                            forks.get(right).opusc();
                            System.out.println(Thread.currentThread().getName() + ": Podnosi na prawy widelec (fork " + right + ")");
                        } else {
                            System.out.println(Thread.currentThread().getName() + ": Chce podniesc lewy widelec (fork " + left + ")");
                            forks.get(left).opusc();
                            System.out.println(Thread.currentThread().getName() + ": Podnosi lewy widelec (fork " + left + ")");
                        }

                        try {
                            System.out.println(Thread.currentThread().getName() + ": ...pauza przed wzięciem lewego widelca...");
                            Thread.sleep(300); // 300ms pause
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (id % 2 == 0) {
                            System.out.println(Thread.currentThread().getName() + ": Chce podniesc lewy widelec (fork " + left + ")");
                            forks.get(left).opusc();
                            System.out.println(Thread.currentThread().getName() + ": Podnosi lewy widelec (fork " + left + ")");
                        } else {
                            System.out.println(Thread.currentThread().getName() + ": Chce podniesc prawy widelec (fork " + right + ")");
                            forks.get(right).opusc();
                            System.out.println(Thread.currentThread().getName() + ": Podnosi prawy widelec (fork " + right + ")");
                        }

                        // 3. EAT
                        try {
                            System.out.println(Thread.currentThread().getName() + ": ZACZYNA JEŚĆ");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 4. Release forks
                        System.out.println(Thread.currentThread().getName() + ": Opuszcza oba widelce");
                        forks.get(right).podnies();
                        forks.get(left).podnies();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Philosopher " + id));
        }
        for (Thread thread : philosophers) {
            thread.start();
        }

        for (Thread thread : philosophers) {
            thread.join();
        }
    }

    public void sol3() throws InterruptedException {
        // Kelner pozwala wejść N-1 kelnerom do stołu gdzie N to liczba kelnerów
        CountSemaphore waiter = new CountSemaphore(philosophers.size() - 1);

        for (int i = 0; i < philosophers.size(); i++) {
            final int id = i;
            philosophers.set(i, new Thread(() -> {
                int right = id;
                int left = (id + 1) % forks.size();
                while (true) {

                    // 1. THINK
                    try {
                        System.out.println(Thread.currentThread().getName() + ": Myśli...");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        // 2. Get into the table
                        System.out.println(Thread.currentThread().getName() + ": CHCE USIASC do stołu...");
                        waiter.opusc();
                        System.out.println(Thread.currentThread().getName() + ": USIADŁ do stołu...");

                        // 3. Grab right fork
                        System.out.println(Thread.currentThread().getName() + ": CHCE wziąć PRAWY widelec (fork " + right + ")");
                        forks.get(right).opusc();
                        System.out.println(Thread.currentThread().getName() + ": WZIAL PRAWY widelec (fork " + right + ")");
                        // 4. Grab left fork
                        System.out.println(Thread.currentThread().getName() + ": CHCE wziąć LEWY widelec (fork " + left + ")");
                        forks.get(left).opusc();
                        System.out.println(Thread.currentThread().getName() + ": WZIAL LEWY widelec (fork " + left + ")");

                        // 5. Eat the food (sleep)
                        System.out.println(Thread.currentThread().getName() + ": ZACZYNA JEŚĆ");
                        Thread.sleep(500);

                        // 6. Drop the forks and leave the table
                        System.out.println(Thread.currentThread().getName() + ": OPUSZCZA oba widelce i odchodzi od stolika");
                        forks.get(right).podnies();
                        forks.get(left).podnies();
                        waiter.podnies();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Philosopher " + id));
        }
        for (Thread thread : philosophers) {
            thread.start();
        }
        for (Thread thread : philosophers) {
            thread.join();
        }
    }

}
