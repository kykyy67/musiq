package by.aleksandr.music.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {
    private Long id;
    private String title;
    private int releaseYear;
    private Long artistId;
    private List<Long> genreIds;
    private List<TrackResponse> tracks;
}
