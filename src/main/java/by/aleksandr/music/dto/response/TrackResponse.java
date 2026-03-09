package by.aleksandr.music.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackResponse {

    private Long id;
    private String title;
    private Integer durationSeconds;
    private Long albumId;
    private Long artistId;
}
