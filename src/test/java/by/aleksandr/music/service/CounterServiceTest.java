package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CounterServiceTest {

    private CounterService counterService;

    @BeforeEach
    void setUp() {
        counterService = new CounterService();
        counterService.reset();
    }

    @Test
    void atomicAndSynchronizedCountersShouldRemainConsistentUnderConcurrency() throws Exception {
        int threadCount = 64;
        int iterationsPerThread = 1_000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        Future<?>[] futures = new Future<?>[threadCount];

        for (int index = 0; index < threadCount; index++) {
            futures[index] = executorService.submit(() -> {
                startSignal.await();
                for (int iteration = 0; iteration < iterationsPerThread; iteration++) {
                    counterService.incrementAtomic(1);
                    counterService.incrementSynchronized(1);
                }
                return null;
            });
        }

        startSignal.countDown();
        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();
        assertThat(executorService.awaitTermination(3, TimeUnit.SECONDS)).isTrue();
        assertThat(counterService.getAtomicValue()).isEqualTo(threadCount * iterationsPerThread);
        assertThat(counterService.getSynchronizedValue()).isEqualTo(threadCount * iterationsPerThread);
    }
}
