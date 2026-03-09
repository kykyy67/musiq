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
        return new ArtistResponse(artist.getId(), artist.getName());
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
