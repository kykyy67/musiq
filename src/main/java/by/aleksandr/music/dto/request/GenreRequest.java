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
@Schema(description = "Genre create/update request")
public class GenreRequest {

    @NotBlank(message = "Genre name must not be blank")
    @Size(max = 100, message = "Genre name must be at most 100 characters")
    @Schema(description = "Genre name", example = "Alternative Rock")
    private String name;
}
