package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.entity.Genre;
import by.aleksandr.music.repository.GenreRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final AlbumSearchCache albumSearchCache;

    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    public List<Genre> findByName(String name) {
        if (name == null || name.isBlank()) {
            return findAll();
        }
        return genreRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<Genre> findById(Long id) {
        return genreRepository.findById(id);
    }

    @Transactional
    public Genre create(Genre genre) {
        Genre saved = genreRepository.save(genre);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public Optional<Genre> update(Long id, Genre genre) {
        return genreRepository.findById(id)
                .map(existing -> {
                    existing.setName(genre.getName());
                    Genre saved = genreRepository.save(existing);
                    albumSearchCache.invalidateAll();
                    return saved;
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (genreRepository.existsById(id)) {
            genreRepository.deleteById(id);
            albumSearchCache.invalidateAll();
            return true;
        }
        return false;
    }
}
