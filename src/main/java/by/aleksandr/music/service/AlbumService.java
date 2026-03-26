package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.AlbumRequest;
import by.aleksandr.music.dto.response.AlbumResponse;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Artist;
import by.aleksandr.music.entity.Genre;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.AlbumMapper;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.ArtistRepository;
import by.aleksandr.music.repository.GenreRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    public java.util.Optional<Album> getAlbumById(Long id) {
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
            log.info("Cache hit for {}", key);
            return cached;
        }
        log.info("Cache miss for {}", key);

        String trackPatternLower = normalizeTrackPattern(trackTitle);
        String genreNameLower = normalize(genreName);

        Page<Long> page = nativeQuery
                ? albumRepository.searchAlbumIdsNative(genreNameLower, trackPatternLower, pageable)
                : albumRepository.searchAlbumIdsJpql(genreNameLower, trackPatternLower, pageable);

        List<Album> albums = fetchAlbumsForPage(page.getContent());

        PagedResponse<AlbumResponse> result = new PagedResponse<>(
                albums.stream().map(AlbumMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        albumSearchCache.put(key, result);
        return result;
    }

    @Transactional
    public Album create(AlbumRequest request) {
        Album album = Album.builder()
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .artists(resolveArtists(request.getArtistIds()))
                .genres(resolveGenres(request.getGenreIds()))
                .build();
        Album saved = albumRepository.save(album);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public Album update(Long id, AlbumRequest request) {
        Album existing = albumRepository.findWithEntityGraphById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album with id " + id + " not found"));

        existing.setTitle(request.getTitle());
        existing.setReleaseYear(request.getReleaseYear());
        existing.getArtists().clear();
        existing.getArtists().addAll(resolveArtists(request.getArtistIds()));
        existing.getGenres().clear();
        existing.getGenres().addAll(resolveGenres(request.getGenreIds()));

        Album saved = albumRepository.save(existing);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        Album album = albumRepository.findForDeleteById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album with id " + id + " not found"));
        album.getTracks().forEach(track ->
                track.getUsers().forEach(user -> user.getTracks().remove(track)));
        albumRepository.delete(album);
        albumSearchCache.invalidateAll();
    }

    private Set<Artist> resolveArtists(List<Long> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Artist> artists = new HashSet<>(artistRepository.findAllById(artistIds));
        if (artists.size() != new HashSet<>(artistIds).size()) {
            throw new BadRequestException("One or more artist ids do not exist");
        }
        return artists;
    }

    private Set<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(genreIds));
        if (genres.size() != new HashSet<>(genreIds).size()) {
            throw new BadRequestException("One or more genre ids do not exist");
        }
        return genres;
    }

    private String normalizeTrackPattern(String trackTitle) {
        String normalized = normalize(trackTitle);
        return normalized == null ? null : "%" + normalized + "%";
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private List<Album> fetchAlbumsForPage(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> order = java.util.stream.IntStream.range(0, ids.size())
                .boxed()
                .collect(java.util.stream.Collectors.toMap(ids::get, Function.identity()));

        return albumRepository.findByIdIn(ids).stream()
                .sorted(Comparator.comparingInt(album -> order.getOrDefault(album.getId(), Integer.MAX_VALUE)))
                .toList();
    }
}
