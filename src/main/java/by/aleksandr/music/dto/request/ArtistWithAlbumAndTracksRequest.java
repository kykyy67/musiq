package by.aleksandr.music.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistWithAlbumAndTracksRequest {

    private String artistName;
    private String albumTitle;
    private int releaseYear;
    private List<TrackItem> tracks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackItem {
        private String title;
        private Integer durationSeconds;
    }
}

