package by.aleksandr.music.service;

import by.aleksandr.music.dto.response.AsyncTaskStatusResponse;
import by.aleksandr.music.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskRegistry {

    private final AtomicLong taskIdSequence = new AtomicLong();
    private final ConcurrentMap<Long, TaskSnapshot> tasks = new ConcurrentHashMap<>();

    public long createTask(int totalSteps) {
        long taskId = taskIdSequence.incrementAndGet();
        tasks.put(taskId, new TaskSnapshot(
                taskId,
                AsyncTaskState.SUBMITTED,
                Instant.now(),
                null,
                null,
                0,
                0,
                totalSteps,
                0,
                0,
                null));
        return taskId;
    }

    public AsyncTaskStatusResponse getTask(long taskId) {
        TaskSnapshot snapshot = tasks.get(taskId);
        if (snapshot == null) {
            throw new ResourceNotFoundException("Task with id " + taskId + " not found");
        }
        return snapshot.toResponse();
    }

    public void markRunning(long taskId) {
        tasks.compute(taskId, (ignored, current) -> requireTask(current, taskId).withRunning());
    }

    public void markProgress(long taskId, int completedSteps, int safeCounterValue) {
        tasks.compute(taskId, (ignored, current) -> requireTask(current, taskId)
                .withProgress(completedSteps, safeCounterValue));
    }

    public void markCompleted(long taskId, int totalAppliedIncrements, int safeCounterValue) {
        tasks.compute(taskId, (ignored, current) -> requireTask(current, taskId)
                .withCompleted(totalAppliedIncrements, safeCounterValue));
    }

    public void markFailed(long taskId, String errorMessage) {
        tasks.compute(taskId, (ignored, current) -> requireTask(current, taskId).withFailure(errorMessage));
    }

    private TaskSnapshot requireTask(TaskSnapshot current, long taskId) {
        if (current == null) {
            throw new ResourceNotFoundException("Task with id " + taskId + " not found");
        }
        return current;
    }

    private enum AsyncTaskState {
        SUBMITTED,
        RUNNING,
        COMPLETED,
        FAILED
    }

    private record TaskSnapshot(
            long taskId,
            AsyncTaskState state,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            int progressPercent,
            int completedSteps,
            int totalSteps,
            int safeCounterValue,
            int totalAppliedIncrements,
            String errorMessage) {

        private TaskSnapshot withRunning() {
            return new TaskSnapshot(
                    taskId,
                    AsyncTaskState.RUNNING,
                    createdAt,
                    startedAt == null ? Instant.now() : startedAt,
                    completedAt,
                    progressPercent,
                    completedSteps,
                    totalSteps,
                    safeCounterValue,
                    totalAppliedIncrements,
                    errorMessage);
        }

        private TaskSnapshot withProgress(int newCompletedSteps, int newSafeCounterValue) {
            int newProgressPercent = totalSteps == 0 ? 100 : (newCompletedSteps * 100) / totalSteps;
            return new TaskSnapshot(
                    taskId,
                    AsyncTaskState.RUNNING,
                    createdAt,
                    startedAt == null ? Instant.now() : startedAt,
                    null,
                    newProgressPercent,
                    newCompletedSteps,
                    totalSteps,
                    newSafeCounterValue,
                    totalAppliedIncrements,
                    null);
        }

        private TaskSnapshot withCompleted(int newTotalAppliedIncrements, int newSafeCounterValue) {
            return new TaskSnapshot(
                    taskId,
                    AsyncTaskState.COMPLETED,
                    createdAt,
                    startedAt == null ? Instant.now() : startedAt,
                    Instant.now(),
                    100,
                    totalSteps,
                    totalSteps,
                    newSafeCounterValue,
                    newTotalAppliedIncrements,
                    null);
        }

        private TaskSnapshot withFailure(String newErrorMessage) {
            return new TaskSnapshot(
                    taskId,
                    AsyncTaskState.FAILED,
                    createdAt,
                    startedAt == null ? Instant.now() : startedAt,
                    Instant.now(),
                    progressPercent,
                    completedSteps,
                    totalSteps,
                    safeCounterValue,
                    totalAppliedIncrements,
                    newErrorMessage);
        }

        private AsyncTaskStatusResponse toResponse() {
            return new AsyncTaskStatusResponse(
                    taskId,
                    state.name(),
                    createdAt,
                    startedAt,
                    completedAt,
                    progressPercent,
                    completedSteps,
                    totalSteps,
                    safeCounterValue,
                    totalAppliedIncrements,
                    errorMessage);
        }
    }
}
