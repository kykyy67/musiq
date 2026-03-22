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
@Schema(description = "User response")
public class UserResponse {

    @Schema(description = "User identifier", example = "1")
    private Long id;

    @Schema(description = "User name", example = "Alex")
    private String name;

    @ArraySchema(schema = @Schema(description = "Track identifier", example = "10"))
    private List<Long> trackIds;
}
