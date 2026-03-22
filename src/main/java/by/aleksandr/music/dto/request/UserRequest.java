package by.aleksandr.music.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User create/update request")
public class UserRequest {

    @NotBlank(message = "User name must not be blank")
    @Size(max = 255, message = "User name must be at most 255 characters")
    @Schema(description = "User name", example = "Alex")
    private String name;

    @ArraySchema(schema = @Schema(description = "Track identifier", example = "10"))
    private List<Long> trackIds;
}
