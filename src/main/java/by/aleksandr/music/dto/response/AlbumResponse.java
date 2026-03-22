package by.aleksandr.music.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Album response")
public class AlbumResponse {

    @Schema(description = "Album identifier", example = "1")
    private Long id;

    @Schema(description = "Album title", example = "Hybrid Theory")
    private String title;

    @Schema(description = "Album release year", example = "2000")
    private int releaseYear;

    @ArraySchema(schema = @Schema(description = "Artist identifier", example = "1"))
    private List<Long> artistIds;

    @ArraySchema(schema = @Schema(description = "Genre identifier", example = "2"))
    private List<Long> genreIds;

    @ArraySchema(schema = @Schema(implementation = TrackResponse.class))
    private List<TrackResponse> tracks;
}
