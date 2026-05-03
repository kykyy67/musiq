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
        }
        if (track.getArtist() != null) {
            response.setArtistId(track.getArtist().getId());
        }
        if (track.getGenre() != null) {
            response.setGenreId(track.getGenre().getId());
        }
        return response;
    }

    public static List<TrackResponse> toResponseList(List<Track> tracks) {
        return tracks.stream().map(TrackMapper::toResponse).toList();
    }
}
