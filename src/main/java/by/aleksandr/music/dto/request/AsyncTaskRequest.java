package by.aleksandr.music.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AsyncTaskRequest(
        @Min(1) @Max(100) Integer steps,
        @Min(10) @Max(5_000) Long delayMillis,
        @Min(1) @Max(10_000) Integer incrementPerStep) {
}
