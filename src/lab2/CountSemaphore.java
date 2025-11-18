package lab2;

public class CountSemaphore {
    private int counter;

    public CountSemaphore(int capacity) {
        counter = capacity;
    }

    public synchronized void opusc() throws InterruptedException {
        while (counter == 0) {
            wait();
        }
        counter--;
    }

    public synchronized void podnies() {
        counter++;
        notifyAll();
    }

    public int getCounter() {
        return counter;
    }
}
