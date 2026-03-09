package by.aleksandr.music.mapper;

import by.aleksandr.music.dto.response.AlbumResponse;
import by.aleksandr.music.dto.response.TrackResponse;
import by.aleksandr.music.entity.Album;
import java.util.List;

public final class AlbumMapper {

    private AlbumMapper() {
    }

    public static AlbumResponse toResponse(Album album) {
        if (album == null) {
            return null;
        }

        Long artistId = album.getArtist() != null ? album.getArtist().getId() : null;

        List<Long> genreIds = album.getGenres() == null
                ? List.of()
                : album.getGenres().stream().map(g -> g.getId()).toList();

        Long currentAlbumId = album.getId();

        List<TrackResponse> trackResponses = album.getTracks() == null
                ? List.of()
                : album.getTracks().stream()
                .map(t -> new TrackResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getDurationSeconds(),
                        album.getId(),
                        album.getArtist() != null ? album.getArtist().getId() : null
                ))
                .toList();

        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                artistId,
                genreIds,
                trackResponses);
    }

    public static List<AlbumResponse> toResponseList(List<Album> albums) {
        if (albums == null) {
            return List.of();
        }
        return albums.stream().map(AlbumMapper::toResponse).toList();
    }
}