package by.aleksandr.music.mapper;

import by.aleksandr.music.dto.request.GenreRequest;
import by.aleksandr.music.dto.response.GenreResponse;
import by.aleksandr.music.entity.Genre;
import java.util.List;

public final class GenreMapper {

    private GenreMapper() {
    }

    public static GenreResponse toResponse(Genre genre) {
        if (genre == null) {
            return null;
        }
        return new GenreResponse(genre.getId(), genre.getName());
    }

    public static List<GenreResponse> toResponseList(List<Genre> genres) {
        return genres.stream().map(GenreMapper::toResponse).toList();
    }

    public static Genre toEntity(GenreRequest request) {
        if (request == null) {
            return null;
        }
        return Genre.builder()
                .name(request.getName())
                .build();
    }
}
