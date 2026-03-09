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

    public List<Album> getAllAlbumsWithArtistAndGenres() {
        return albumRepository.findAll();
    }

    public List<Album> getAlbumsByTitleWithArtistAndGenres(String title) {
        return getAlbumsByTitle(title);
    }

    public Optional<Album> getAlbumById(Long id) {
        return albumRepository.findById(id);
    }

    @Transactional
    public Optional<Album> create(AlbumRequest request) {
        Artist artist = request.getArtistId() != null
                ? artistRepository.findById(request.getArtistId()).orElse(null)
                : null;

        Set<Genre> genres = resolveGenres(request.getGenreIds());

        Album album = Album.builder()
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .artist(artist)
                .genres(genres)
                .build();

        return Optional.of(albumRepository.save(album));
    }

    @Transactional
    public Optional<Album> update(Long id, AlbumRequest request) {
        return albumRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(request.getTitle());
                    existing.setReleaseYear(request.getReleaseYear());

                    if (request.getArtistId() != null) {
                        artistRepository.findById(request.getArtistId()).ifPresent(existing::setArtist);
                    } else {
                        existing.setArtist(null);
                    }

                    // Обновляем связи с жанрами
                    existing.getGenres().clear();
                    existing.getGenres().addAll(resolveGenres(request.getGenreIds()));

                    return albumRepository.save(existing);
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        return albumRepository.findById(id)
                .map(album -> {
                    album.getTracks().forEach(track -> {
                        track.getUsers().forEach(user -> user.getTracks().remove(track));
                    });

                    albumRepository.delete(album);
                    return true;
                })
                .orElse(false);
    }

    private Set<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(genreRepository.findAllById(genreIds));
    }
}