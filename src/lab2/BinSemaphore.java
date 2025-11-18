package lab2;

public class BinSemaphore {
    private boolean busy = false;

    public synchronized void opusc() throws InterruptedException {
        while (busy) {
            wait();
        }
        busy = true;
    }

    public synchronized void podnies() {
        busy = false;
        notifyAll();
    }
}
