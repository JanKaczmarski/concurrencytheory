package lab1;

public class Counter {
    public int counter = 0;
    public synchronized void increment() {
        this.counter++;
    }
    public synchronized void decrement() {
        this.counter--;
    }
}
