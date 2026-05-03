package by.aleksandr.music.mapper;

import by.aleksandr.music.dto.request.ArtistRequest;
import by.aleksandr.music.dto.response.ArtistResponse;
import by.aleksandr.music.entity.Artist;
import java.util.List;

public final class ArtistMapper {

    private ArtistMapper() {
    }

    public static ArtistResponse toResponse(Artist artist) {
        if (artist == null) {
            return null;
        }
        List<Long> albumIds = artist.getAlbums() == null
                ? List.of()
                : artist.getAlbums().stream().map(album -> album.getId()).toList();
        return new ArtistResponse(artist.getId(), artist.getName(), albumIds);
    }

    public static List<ArtistResponse> toResponseList(List<Artist> artists) {
        return artists.stream().map(ArtistMapper::toResponse).toList();
    }

    public static Artist toEntity(ArtistRequest request) {
        if (request == null) {
            return null;
        }
        return Artist.builder()
                .name(request.getName())
                .build();
    }
}
