package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.BulkTrackItemRequest;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
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

    public Optional<Track> findById(Long id) {
        return trackRepository.findById(id);
    }

    @Transactional
    public Track create(String title, Integer durationSeconds, Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException(ALBUM_WITH_ID + albumId + NOT_FOUND));
        Track track = Track.builder()
                .title(title)
                .durationSeconds(durationSeconds)
                .album(album)
                .build();
        Track saved = trackRepository.save(track);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public Track update(Long id, String title, Integer durationSeconds, Long albumId) {
        Track existing = trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_WITH_ID + id + NOT_FOUND));
        existing.setTitle(title);
        existing.setDurationSeconds(durationSeconds);
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException(ALBUM_WITH_ID + albumId + NOT_FOUND));
        existing.setAlbum(album);
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
                .orElseThrow(() -> new ResourceNotFoundException("Album with id " + albumId + " not found"));

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
}
