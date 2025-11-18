package lab2;

/**
 * A simple class that simulates a parking lot to demonstrate the functionality of a counting semaphore.
 * It's used to control access of concurrent threads ('cars') to a limited pool of resources ('parking spaces').
 */
public class Parking {
    private CountSemaphore semaphore;

    public Parking(CountSemaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void enter(String carName) {
        try {
            System.out.println("[" + carName + "] podjeżdża i czeka na wolne miejsce...");

            semaphore.opusc();
            System.out.println("[" + carName + "] ZAPARKOWAŁ. Pozostało wolnych miejsc w semaforze: " + semaphore.getCounter());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void leave(String carName) {
        System.out.println("[" + carName + "] opuszcza parking.");

        semaphore.podnies();

        System.out.println("Miejsce zwolnione przez [" + carName + "]. Wolne miejsca w semaforze: " + semaphore.getCounter());
    }
}
