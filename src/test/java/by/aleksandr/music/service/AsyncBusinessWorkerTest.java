package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsyncBusinessWorkerTest {

    @Mock
    private AsyncTaskRegistry asyncTaskRegistry;

    @Mock
    private CounterService counterService;

    @Test
    void executeTaskShouldMarkTaskFailedWhenInterrupted() {
        AsyncBusinessWorker worker = new AsyncBusinessWorker(asyncTaskRegistry, counterService);
        Thread.currentThread().interrupt();

        CompletableFuture<Void> future = worker.executeTask(11L, 1, 1_000L, 1);

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        verify(asyncTaskRegistry).markRunning(11L);
        verify(asyncTaskRegistry).markFailed(11L, "Task was interrupted");
        assertThatThrownBy(future::join).hasCauseInstanceOf(InterruptedException.class);
        Thread.interrupted();
    }

    @Test
    void executeTaskShouldMarkTaskFailedWhenRuntimeExceptionOccurs() {
        AsyncBusinessWorker worker = new AsyncBusinessWorker(asyncTaskRegistry, counterService);
        when(counterService.incrementAtomic(5)).thenThrow(new IllegalStateException("Boom"));

        CompletableFuture<Void> future = worker.executeTask(12L, 1, 0L, 5);

        verify(asyncTaskRegistry).markRunning(12L);
        verify(asyncTaskRegistry).markFailed(12L, "Boom");
        assertThatThrownBy(future::join).hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void executeTaskShouldCompleteAndStoreFinalCounterValue() throws ExecutionException, InterruptedException {
        AsyncBusinessWorker worker = new AsyncBusinessWorker(asyncTaskRegistry, counterService);
        when(counterService.incrementAtomic(3)).thenReturn(3);
        when(counterService.getAtomicValue()).thenReturn(3);

        CompletableFuture<Void> future = worker.executeTask(13L, 1, 0L, 3);

        future.get();
        verify(asyncTaskRegistry).markRunning(13L);
        verify(asyncTaskRegistry).markProgress(13L, 1, 3);
        verify(asyncTaskRegistry).markCompleted(13L, 3, 3);
    }
}
