package by.aleksandr.music.service;

import by.aleksandr.music.dto.response.RaceConditionDemoResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class RaceConditionDemoService {

    public RaceConditionDemoResponse runDemo(int threadCount, int iterationsPerThread) {
        PlainCounter unsafeCounter = new PlainCounter();
        AtomicInteger atomicCounter = new AtomicInteger();
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        int expectedTotal = threadCount * iterationsPerThread;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        long startedAt = System.nanoTime();

        try {
            for (int index = 0; index < threadCount; index++) {
                futures.add(executorService.submit(() -> {
                    awaitStart(startSignal);
                    for (int iteration = 0; iteration < iterationsPerThread; iteration++) {
                        unsafeCounter.increment();
                        atomicCounter.incrementAndGet();
                        synchronizedCounter.increment();
                    }
                }));
            }

            startSignal.countDown();
            for (java.util.concurrent.Future<?> future : futures) {
                future.get();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Race condition demo failed", exception);
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
            }
        }

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        return new RaceConditionDemoResponse(
                threadCount,
                iterationsPerThread,
                expectedTotal,
                unsafeCounter.get(),
                atomicCounter.get(),
                synchronizedCounter.get(),
                elapsedMillis);
    }

    private void awaitStart(CountDownLatch startSignal) {
        try {
            startSignal.await();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", interruptedException);
        }
    }

    private static final class PlainCounter {
        private int value;

        private void increment() {
            int current = value;
            if ((current & 127) == 0) {
                Thread.yield();
            }
            value = current + 1;
        }

        private int get() {
            return value;
        }
    }

    private static final class SynchronizedCounter {
        private int value;

        private synchronized void increment() {
            value++;
        }

        private synchronized int get() {
            return value;
        }
    }
}
