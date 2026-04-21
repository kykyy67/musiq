package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import by.aleksandr.music.dto.response.AsyncTaskStatusResponse;
import by.aleksandr.music.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class AsyncTaskRegistryTest {

    @Test
    void createTaskShouldReturnSubmittedSnapshot() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();

        long taskId = registry.createTask(4);
        AsyncTaskStatusResponse task = registry.getTask(taskId);

        assertThat(task.taskId()).isEqualTo(taskId);
        assertThat(task.status()).isEqualTo("SUBMITTED");
        assertThat(task.totalSteps()).isEqualTo(4);
        assertThat(task.startedAt()).isNull();
        assertThat(task.completedAt()).isNull();
    }

    @Test
    void markRunningShouldPreserveStartedAtWhenCalledTwice() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();
        long taskId = registry.createTask(2);

        registry.markRunning(taskId);
        AsyncTaskStatusResponse first = registry.getTask(taskId);
        registry.markRunning(taskId);
        AsyncTaskStatusResponse second = registry.getTask(taskId);

        assertThat(first.status()).isEqualTo("RUNNING");
        assertThat(first.startedAt()).isNotNull();
        assertThat(second.startedAt()).isEqualTo(first.startedAt());
    }

    @Test
    void markProgressShouldCalculateProgressAndHandleZeroTotalSteps() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();
        long taskId = registry.createTask(0);

        registry.markProgress(taskId, 0, 7);
        AsyncTaskStatusResponse task = registry.getTask(taskId);

        assertThat(task.status()).isEqualTo("RUNNING");
        assertThat(task.progressPercent()).isEqualTo(100);
        assertThat(task.safeCounterValue()).isEqualTo(7);
        assertThat(task.errorMessage()).isNull();
    }

    @Test
    void markCompletedShouldFillCompletedFieldsEvenWithoutRunningState() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();
        long taskId = registry.createTask(3);

        registry.markCompleted(taskId, 15, 15);
        AsyncTaskStatusResponse task = registry.getTask(taskId);

        assertThat(task.status()).isEqualTo("COMPLETED");
        assertThat(task.startedAt()).isNotNull();
        assertThat(task.completedAt()).isNotNull();
        assertThat(task.progressPercent()).isEqualTo(100);
        assertThat(task.totalAppliedIncrements()).isEqualTo(15);
    }

    @Test
    void markCompletedShouldPreserveStartedAtWhenTaskIsAlreadyRunning() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();
        long taskId = registry.createTask(2);

        registry.markRunning(taskId);
        AsyncTaskStatusResponse runningTask = registry.getTask(taskId);
        registry.markCompleted(taskId, 4, 4);
        AsyncTaskStatusResponse completedTask = registry.getTask(taskId);

        assertThat(completedTask.startedAt()).isEqualTo(runningTask.startedAt());
    }

    @Test
    void markFailedShouldFillFailureFieldsEvenWithoutRunningState() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();
        long taskId = registry.createTask(1);

        registry.markFailed(taskId, "Failure");
        AsyncTaskStatusResponse task = registry.getTask(taskId);

        assertThat(task.status()).isEqualTo("FAILED");
        assertThat(task.startedAt()).isNotNull();
        assertThat(task.completedAt()).isNotNull();
        assertThat(task.errorMessage()).isEqualTo("Failure");
    }

    @Test
    void markFailedShouldPreserveStartedAtWhenTaskIsAlreadyRunning() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();
        long taskId = registry.createTask(1);

        registry.markRunning(taskId);
        AsyncTaskStatusResponse runningTask = registry.getTask(taskId);
        registry.markFailed(taskId, "Failure");
        AsyncTaskStatusResponse failedTask = registry.getTask(taskId);

        assertThat(failedTask.startedAt()).isEqualTo(runningTask.startedAt());
    }

    @Test
    void getTaskShouldThrowWhenTaskDoesNotExist() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();

        assertThatThrownBy(() -> registry.getTask(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task with id 99 not found");
    }

    @Test
    void markMethodsShouldThrowWhenTaskDoesNotExist() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry();

        assertThatThrownBy(() -> registry.markRunning(100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task with id 100 not found");
        assertThatThrownBy(() -> registry.markProgress(100L, 1, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task with id 100 not found");
        assertThatThrownBy(() -> registry.markCompleted(100L, 1, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task with id 100 not found");
        assertThatThrownBy(() -> registry.markFailed(100L, "x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task with id 100 not found");
    }
}
