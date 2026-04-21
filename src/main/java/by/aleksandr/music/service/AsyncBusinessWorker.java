package by.aleksandr.music.service;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncBusinessWorker {

    private final AsyncTaskRegistry asyncTaskRegistry;
    private final CounterService counterService;

    public AsyncBusinessWorker(AsyncTaskRegistry asyncTaskRegistry, CounterService counterService) {
        this.asyncTaskRegistry = asyncTaskRegistry;
        this.counterService = counterService;
    }

    @Async("businessTaskExecutor")
    public CompletableFuture<Void> executeTask(long taskId, int steps, long delayMillis, int incrementPerStep) {
        try {
            asyncTaskRegistry.markRunning(taskId);
            for (int step = 1; step <= steps; step++) {
                Thread.sleep(delayMillis);
                int safeCounterValue = counterService.incrementAtomic(incrementPerStep);
                asyncTaskRegistry.markProgress(taskId, step, safeCounterValue);
            }
            asyncTaskRegistry.markCompleted(taskId, steps * incrementPerStep, counterService.getAtomicValue());
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            asyncTaskRegistry.markFailed(taskId, "Task was interrupted");
            return CompletableFuture.failedFuture(interruptedException);
        } catch (RuntimeException runtimeException) {
            asyncTaskRegistry.markFailed(taskId, runtimeException.getMessage());
            return CompletableFuture.failedFuture(runtimeException);
        }
    }
}
