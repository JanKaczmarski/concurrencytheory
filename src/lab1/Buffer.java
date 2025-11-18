package lab1;

public class Buffer {
    private final String[] buf = {""};

    public synchronized void put(String value) {
        /*
         Używam instruckji while zamiast instruckji If gdyż:
         Jeśli kilka wątków zostanie wybudzonych za pomocą notifyAll(); to wtedy możliwym jest, że
         zanim ten wątek się obudzi to inny już zostanie przydzielony do monitora (race condition). Dlatego po przebudzeniu
         jeszcze raz sprawdzam czy warunek dalej jest spełniony - jeśli nie jest to czekam dalej.

         Tutaj element z wikipedi:
         "In computing, a spurious wakeup occurs when a thread wakes up from waiting on a condition variable and
         finds that the condition is still unsatisfied. It is referred to as spurious because the thread has seemingly
         been awoken for no reason. Spurious wakeups usually happen because in between the time when the
         condition variable was signaled and when the awakened thread was finally able to run, another thread
         ran first and changed the condition again. In general, if multiple threads are awakened, the first one to run
         will find the condition satisfied, but the others may find the condition unsatisfied. In this way, there is
         a race condition between all the awakened threads. The first thread to run will win the race and find the
         condition satisfied, while the other threads will lose the race, and experience a spurious wakeup."
        */
        while (!buf[0].isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("PUT: " + value);
        buf[0] = value;
        notifyAll();
    }
    public synchronized String take() {
        while (buf[0].isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String out = buf[0];
        buf[0] = "";
        System.out.println("TAKE: " + out);
        notifyAll();
        return out;
    }
}
