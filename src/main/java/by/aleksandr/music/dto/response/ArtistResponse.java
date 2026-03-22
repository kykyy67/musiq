package by.aleksandr.music.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Artist response")
public class ArtistResponse {

    @Schema(description = "Artist identifier", example = "1")
    private Long id;

    @Schema(description = "Artist name", example = "Linkin Park")
    private String name;
}
