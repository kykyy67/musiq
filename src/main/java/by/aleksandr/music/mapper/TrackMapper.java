package by.aleksandr.music.mapper;

import by.aleksandr.music.dto.response.TrackResponse;
import by.aleksandr.music.entity.Track;
import java.util.List;

public final class TrackMapper {

    private TrackMapper() {
    }

    public static TrackResponse toResponse(Track track) {
        if (track == null) {
            return null;
        }

        TrackResponse response = new TrackResponse();
        response.setId(track.getId());
        response.setTitle(track.getTitle());
        response.setDurationSeconds(track.getDurationSeconds());

        if (track.getAlbum() != null) {
            response.setAlbumId(track.getAlbum().getId());

            if (track.getAlbum().getArtists() != null && !track.getAlbum().getArtists().isEmpty()) {
                Long firstArtistId = track.getAlbum().getArtists().iterator().next().getId();
                response.setArtistId(firstArtistId);
            }
        }
        return response;
    }

    public static List<TrackResponse> toResponseList(List<Track> tracks) {
        return tracks.stream().map(TrackMapper::toResponse).toList();
    }
}
