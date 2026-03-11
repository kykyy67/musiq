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

        List<Long> artistIds = (album.getArtists() == null)
                ? List.of()
                : album.getArtists().stream().map(a -> a.getId()).toList();

        List<Long> genreIds = (album.getGenres() == null)
                ? List.of()
                : album.getGenres().stream().map(g -> g.getId()).toList();

        List<TrackResponse> trackResponses = (album.getTracks() == null)
                ? List.of()
                : album.getTracks().stream()
                .map(t -> new TrackResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getDurationSeconds(),
                        album.getId(),
                        artistIds.isEmpty() ? null : artistIds.get(0) // Для трека берем первого артиста или null
                ))
                .toList();

        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                artistIds,
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
