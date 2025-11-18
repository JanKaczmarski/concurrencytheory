package lab1;

public class Consumer implements Runnable {
    private Buffer buffer;
    private final int total;

    public Consumer(Buffer buffer, int total) {
        this.buffer = buffer;
        this.total = total;
    }

    public void run() {
        for(int i = 0;  i < total;   i++) {
            String message = buffer.take();
        }
    }
}
