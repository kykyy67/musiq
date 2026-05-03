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
@Schema(description = "Track create/update request")
public class TrackRequest {

    @NotBlank(message = "Track title must not be blank")
    @Size(max = 255, message = "Track title must be at most 255 characters")
    @Schema(description = "Track title", example = "Numb")
    private String title;

    @NotNull(message = "Track duration is required")
    @Min(value = 1, message = "Track duration must be positive")
    @Schema(description = "Track duration in seconds", example = "187")
    private Integer durationSeconds;

    @Schema(description = "Album identifier", example = "1")
    private Long albumId;

    @Schema(description = "Artist identifier", example = "3")
    private Long artistId;

    @Schema(description = "Genre identifier", example = "4")
    private Long genreId;
}
