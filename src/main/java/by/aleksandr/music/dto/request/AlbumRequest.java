package by.aleksandr.music.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Album create/update request")
public class AlbumRequest {

    @NotBlank(message = "Album title must not be blank")
    @Size(max = 255, message = "Album title must be at most 255 characters")
    @Schema(description = "Album title", example = "Hybrid Theory")
    private String title;

    @NotNull(message = "Release year is required")
    @Min(value = 1900, message = "Release year must be greater than or equal to 1900")
    @Max(value = 2100, message = "Release year must be less than or equal to 2100")
    @Schema(description = "Album release year", example = "2000")
    private Integer releaseYear;

    @ArraySchema(schema = @Schema(description = "Artist identifier", example = "1"))
    private List<Long> artistIds;

    @ArraySchema(schema = @Schema(description = "Genre identifier", example = "2"))
    private List<Long> genreIds;
}
