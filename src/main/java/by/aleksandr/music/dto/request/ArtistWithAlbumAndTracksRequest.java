package by.aleksandr.music.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Composite request for creating artist, album and tracks")
public class ArtistWithAlbumAndTracksRequest {

    @NotBlank(message = "Artist name must not be blank")
    @Size(max = 255, message = "Artist name must be at most 255 characters")
    @Schema(description = "Artist name", example = "Muse")
    private String artistName;

    @NotBlank(message = "Album title must not be blank")
    @Size(max = 255, message = "Album title must be at most 255 characters")
    @Schema(description = "Album title", example = "Absolution")
    private String albumTitle;

    @NotNull(message = "Release year is required")
    @Min(value = 1900, message = "Release year must be greater than or equal to 1900")
    @Max(value = 2100, message = "Release year must be less than or equal to 2100")
    @Schema(description = "Album release year", example = "2003")
    private Integer releaseYear;

    @Valid
    @NotEmpty(message = "Tracks must contain at least one track")
    @ArraySchema(schema = @Schema(implementation = TrackItem.class))
    private List<TrackItem> tracks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Track item inside composite request")
    public static class TrackItem {

        @NotBlank(message = "Track title must not be blank")
        @Size(max = 255, message = "Track title must be at most 255 characters")
        @Schema(description = "Track title", example = "Time Is Running Out")
        private String title;

        @NotNull(message = "Track duration is required")
        @Min(value = 1, message = "Track duration must be positive")
        @Schema(description = "Track duration in seconds", example = "217")
        private Integer durationSeconds;
    }
}
