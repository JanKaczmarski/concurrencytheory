package lab1;

public class Producer implements Runnable {
    private Buffer buffer;
    private int total;

    public Producer(Buffer buffer, int total) {
        this.buffer = buffer;
        this.total = total;
    }

    public void run() {

        for(int i = 0;  i < total;   i++) {
            buffer.put("message "+i);
        }
    }
}
