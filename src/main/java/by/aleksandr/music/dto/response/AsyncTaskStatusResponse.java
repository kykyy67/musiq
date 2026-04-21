package by.aleksandr.music.dto.response;

import java.time.Instant;

public record AsyncTaskStatusResponse(
        Long taskId,
        String status,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt,
        int progressPercent,
        int completedSteps,
        int totalSteps,
        int safeCounterValue,
        int totalAppliedIncrements,
        String errorMessage) {
}
