package by.aleksandr.music.service;

import by.aleksandr.music.dto.request.AlbumRequest;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Artist;
import by.aleksandr.music.entity.Genre;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.ArtistRepository;
import by.aleksandr.music.repository.GenreRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Обеспечивает наличие сессии Hibernate для всех GET методов
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    public List<Album> getAlbumsByTitle(String title) {
        if (title == null || title.isBlank()) {
            return albumRepository.findAll();
        }
        return albumRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Album> getAlbumsByTitleWithArtistAndGenres(String title) {
        return getAlbumsByTitle(title);
    }

    public Optional<Album> getAlbumById(Long id) {
        // ИСПРАВЛЕНО: используем метод с EntityGraph вместо стандартного findById
        return albumRepository.findWithEntityGraphById(id);
    }

    @Transactional
    public Optional<Album> create(AlbumRequest request) {
        Set<Artist> artists = resolveArtists(request.getArtistIds());
        Set<Genre> genres = resolveGenres(request.getGenreIds());

        Album album = Album.builder()
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .artists(artists)
                .genres(genres)
                .build();

        return Optional.of(albumRepository.save(album));
    }

    @Transactional
    public Optional<Album> update(Long id, AlbumRequest request) {
        return albumRepository.findWithEntityGraphById(id) // Тоже используем граф здесь
                .map(existing -> {
                    existing.setTitle(request.getTitle());
                    existing.setReleaseYear(request.getReleaseYear());
                    existing.getArtists().clear();
                    existing.getArtists().addAll(resolveArtists(request.getArtistIds()));
                    existing.getGenres().clear();
                    existing.getGenres().addAll(resolveGenres(request.getGenreIds()));
                    return albumRepository.save(existing);
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        return albumRepository.findById(id)
                .map(album -> {
                    album.getTracks().forEach(track ->
                            track.getUsers().forEach(user -> user.getTracks().remove(track)));
                    albumRepository.delete(album);
                    return true;
                })
                .orElse(false);
    }

    private Set<Artist> resolveArtists(List<Long> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(artistRepository.findAllById(artistIds));
    }

    private Set<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(genreRepository.findAllById(genreIds));
    }
}