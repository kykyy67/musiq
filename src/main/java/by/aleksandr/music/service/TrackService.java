package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.BulkTrackItemRequest;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.dto.response.TrackResponse;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Artist;
import by.aleksandr.music.entity.Genre;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.TrackMapper;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.ArtistRepository;
import by.aleksandr.music.repository.GenreRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrackService {

    private static final String ALBUM_WITH_ID = "Album with id ";
    private static final String TRACK_WITH_ID = "Track with id ";
    private static final String NOT_FOUND = " not found";

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final AlbumSearchCache albumSearchCache;

    public List<Track> findAll() {
        return trackRepository.findAll();
    }

    public List<Track> findByTitle(String title) {
        if (title == null || title.isBlank()) {
            return findAll();
        }
        return trackRepository.findByTitleContainingIgnoreCase(title);
    }

    public PagedResponse<TrackResponse> findPage(String title, Pageable pageable) {
        Page<Track> page = (title == null || title.isBlank())
                ? trackRepository.findAll(pageable)
                : trackRepository.findByTitleContainingIgnoreCase(title, pageable);

        return new PagedResponse<>(
                page.getContent().stream().map(TrackMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public Optional<Track> findById(Long id) {
        return trackRepository.findById(id);
    }

    @Transactional
    public Track create(String title, Integer durationSeconds, Long albumId, Long artistId, Long genreId) {
        Track track = Track.builder()
                .title(title)
                .durationSeconds(durationSeconds)
                .album(resolveAlbum(albumId))
                .artist(resolveArtist(artistId))
                .genre(resolveGenre(genreId))
                .build();
        Track saved = trackRepository.save(track);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public Track update(Long id,
                        String title,
                        Integer durationSeconds,
                        Long albumId,
                        Long artistId,
                        Long genreId) {
        Track existing = trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_WITH_ID + id + NOT_FOUND));
        existing.setTitle(title);
        existing.setDurationSeconds(durationSeconds);
        existing.setAlbum(resolveAlbum(albumId));
        existing.setArtist(resolveArtist(artistId));
        existing.setGenre(resolveGenre(genreId));
        Track saved = trackRepository.save(existing);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_WITH_ID + id + NOT_FOUND));
        track.getUsers().forEach(user -> user.getTracks().remove(track));
        trackRepository.delete(track);
        albumSearchCache.invalidateAll();
    }

    public List<Track> createBulkWithoutTransaction(
            Long albumId,
            List<BulkTrackItemRequest> requests,
            Integer fail) {
        try {
            return createBulk(albumId, requests, fail);
        } finally {
            albumSearchCache.invalidateAll();
        }
    }

    @Transactional
    public List<Track> createBulkWithTransaction(
            Long albumId,
            List<BulkTrackItemRequest> requests,
            Integer fail) {
        try {
            return createBulk(albumId, requests, fail);
        } finally {
            albumSearchCache.invalidateAll();
        }
    }

    private List<Track> createBulk(
            Long albumId,
            List<BulkTrackItemRequest> requests,
            Integer fail) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Track list must not be empty");
        }

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException(ALBUM_WITH_ID + albumId + NOT_FOUND));

        return IntStream.range(0, requests.size())
                .mapToObj(index -> saveBulkTrack(album, requests.get(index), index, fail))
                .sorted(Comparator.comparing(Track::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private Track saveBulkTrack(
            Album album,
            BulkTrackItemRequest request,
            int index,
            Integer fail) {
        Optional.ofNullable(fail)
                .filter(expectedIndex -> expectedIndex == index)
                .ifPresent(expectedIndex -> {
                    throw new BadRequestException("Simulated bulk failure at index " + expectedIndex);
                });

        Track track = Track.builder()
                .title(request.getTitle())
                .durationSeconds(request.getDurationSeconds())
                .album(album)
                .build();
        return trackRepository.save(track);
    }

    private Album resolveAlbum(Long albumId) {
        if (albumId == null) {
            return null;
        }
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException(ALBUM_WITH_ID + albumId + NOT_FOUND));
    }

    private Artist resolveArtist(Long artistId) {
        if (artistId == null) {
            return null;
        }
        return artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with id " + artistId + NOT_FOUND));
    }

    private Genre resolveGenre(Long genreId) {
        if (genreId == null) {
            return null;
        }
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre with id " + genreId + NOT_FOUND));
    }
}
