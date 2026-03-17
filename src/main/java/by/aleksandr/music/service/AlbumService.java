package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.AlbumRequest;
import by.aleksandr.music.dto.response.AlbumResponse;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.mapper.AlbumMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final AlbumSearchCache albumSearchCache;

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
        return albumRepository.findWithEntityGraphById(id);
    }

    public PagedResponse<AlbumResponse> searchAlbumsByGenreAndTrack(
            String genreName,
            String trackTitle,
            boolean nativeQuery,
            Pageable pageable) {

        AlbumSearchCache.Key key = AlbumSearchCache.keyOf(genreName, trackTitle, nativeQuery, pageable);
        PagedResponse<AlbumResponse> cached = albumSearchCache.get(key);
        if (cached != null) {
            log.info("Cache worked: {}", key);
            return cached;
        }
        log.info("Cache not worked: {}", key);

        String trackPattern = null;
        if (trackTitle != null && !trackTitle.trim().isEmpty()) {
            trackPattern = "%" + trackTitle.trim() + "%";
        }

        String genreNameLower = null;
        if (genreName != null && !genreName.trim().isEmpty()) {
            genreNameLower = genreName.trim().toLowerCase();
        }

        String trackPatternLower = null;
        if (trackPattern != null) {
            trackPatternLower = trackPattern.toLowerCase();
        }

        Page<Album> page = nativeQuery
                ? albumRepository.searchAlbumsNative(genreNameLower, trackPatternLower, pageable)
                : albumRepository.searchAlbumsJpql(genreNameLower, trackPatternLower, pageable);

        PagedResponse<AlbumResponse> result = new PagedResponse<>(
                page.getContent().stream().map(AlbumMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        albumSearchCache.put(key, result);
        return result;
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

        Optional<Album> saved = Optional.of(albumRepository.save(album));
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public Optional<Album> update(Long id, AlbumRequest request) {
        return albumRepository.findWithEntityGraphById(id)
                .map(existing -> {
                    existing.setTitle(request.getTitle());
                    existing.setReleaseYear(request.getReleaseYear());
                    existing.getArtists().clear();
                    existing.getArtists().addAll(resolveArtists(request.getArtistIds()));
                    existing.getGenres().clear();
                    existing.getGenres().addAll(resolveGenres(request.getGenreIds()));
                    Album saved = albumRepository.save(existing);
                    albumSearchCache.invalidateAll();
                    return saved;
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        return albumRepository.findById(id)
                .map(album -> {
                    album.getTracks().forEach(track ->
                            track.getUsers().forEach(user -> user.getTracks().remove(track)));
                    albumRepository.delete(album);
                    albumSearchCache.invalidateAll();
                    return true;
                })
                .orElse(false);
    }

    private Set<Artist> resolveArtists(List<Long> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(artistRepository.findAllById(artistIds));
    }

    private Set<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(genreRepository.findAllById(genreIds));
    }
}