package by.aleksandr.music.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Artist create/update request")
public class ArtistRequest {

    @NotBlank(message = "Artist name must not be blank")
    @Size(max = 255, message = "Artist name must be at most 255 characters")
    @Schema(description = "Artist name", example = "Linkin Park")
    private String name;
}
