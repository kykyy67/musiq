package by.aleksandr.music.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Track response")
public class TrackResponse {

    @Schema(description = "Track identifier", example = "1")
    private Long id;

    @Schema(description = "Track title", example = "Numb")
    private String title;

    @Schema(description = "Track duration in seconds", example = "187")
    private Integer durationSeconds;

    @Schema(description = "Album identifier", example = "10")
    private Long albumId;

    @Schema(description = "Artist identifier", example = "3")
    private Long artistId;

    @Schema(description = "Genre identifier", example = "4")
    private Long genreId;
}
