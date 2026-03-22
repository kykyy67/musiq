package by.aleksandr.music.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Genre response")
public class GenreResponse {

    @Schema(description = "Genre identifier", example = "1")
    private Long id;

    @Schema(description = "Genre name", example = "Alternative Rock")
    private String name;
}
