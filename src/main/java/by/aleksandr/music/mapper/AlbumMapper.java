package by.aleksandr.music.mapper;

import by.aleksandr.music.dto.AlbumDto;
import by.aleksandr.music.entity.Album;
import java.util.List;

public final class AlbumMapper {

    private AlbumMapper() {
    }

    public static AlbumDto toDto(Album album) {
        if (album == null) {
            return null;
        }
        return new AlbumDto(album.getId(), album.getTitle(), album.getReleaseYear());
    }

    public static List<AlbumDto> toDtoList(List<Album> albums) {
        return albums.stream()
        .map(AlbumMapper::toDto)
        .toList();
    }
}
