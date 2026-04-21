package by.aleksandr.music.service;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private final AtomicInteger atomicCounter = new AtomicInteger();
    private int synchronizedCounter;

    public int incrementAtomic(int delta) {
        return atomicCounter.addAndGet(delta);
    }

    public synchronized int incrementSynchronized(int delta) {
        synchronizedCounter += delta;
        return synchronizedCounter;
    }

    public int getAtomicValue() {
        return atomicCounter.get();
    }

    public synchronized int getSynchronizedValue() {
        return synchronizedCounter;
    }

    public synchronized void reset() {
        atomicCounter.set(0);
        synchronizedCounter = 0;
    }
}
