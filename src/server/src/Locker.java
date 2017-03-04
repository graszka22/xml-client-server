/**
 * This class allows getting information by many clients
 * simultaneously and locking all threads during file sending
 */
public class Locker {
    private int readers = 0;
    private int writers = 0;
    private int pendingWriters = 0;

    public synchronized void lockReader() {
        while (pendingWriters > 0 || writers > 0) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        ++readers;
    }

    public synchronized void lockWriter() {
        ++pendingWriters;
        while (readers > 0 || writers > 0) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        --pendingWriters;
        ++writers;
    }

    public synchronized void unlockReader() {
        --readers;
        notifyAll();
    }

    public synchronized void unlockWriter() {
        --writers;
        notifyAll();
    }
}
