package by.aleksandr.music.exception;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Unified error response")
public record ApiErrorResponse(
        @Schema(description = "Error timestamp", example = "2026-03-22T12:00:00+03:00")
        OffsetDateTime timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP error reason", example = "Bad Request")
        String error,
        @Schema(description = "Application error message", example = "Validation failed")
        String message,
        @Schema(description = "Request path", example = "/api/albums")
        String path,
        @ArraySchema(
                schema = @Schema(description = "Detailed validation or business error message")
        )
        List<String> details
) {
}
