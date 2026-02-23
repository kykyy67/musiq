package by.aleksandr.music.service;

import by.aleksandr.music.entity.Album;
import by.aleksandr.music.repository.AlbumRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    public List<Album> getAlbumsByTitle(String title) {
        if (title == null || title.isBlank()) {
            return getAllAlbums();
        }
        return albumRepository.findByTitleContainingIgnoreCase(title);
    }

    public Optional<Album> getAlbumById(Long id) {
        return albumRepository.findById(id);
    }
}
