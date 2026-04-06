package by.aleksandr.music.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk track item for album tracklist creation")
public class BulkTrackItemRequest {

    @NotBlank(message = "Track title must not be blank")
    @Size(max = 255, message = "Track title must be at most 255 characters")
    @Schema(description = "Track title", example = "Hysteria")
    private String title;

    @NotNull(message = "Track duration is required")
    @Min(value = 1, message = "Track duration must be positive")
    @Schema(description = "Track duration in seconds", example = "227")
    private Integer durationSeconds;
}
