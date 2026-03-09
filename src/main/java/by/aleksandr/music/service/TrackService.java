package by.aleksandr.music.service;

import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;

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
    public Optional<Track> create(String title, Integer durationSeconds, Long albumId) {
        Optional<Album> albumOpt = albumRepository.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Optional.empty();
        }
        Track track = Track.builder()
                .title(title)
                .durationSeconds(durationSeconds != null ? durationSeconds : 0)
                .album(albumOpt.get())
                .build();
        return Optional.of(trackRepository.save(track));
    }

    @Transactional
    public Optional<Track> update(Long id, String title, Integer durationSeconds, Long albumId) {
        return trackRepository.findById(id)
                .map(existing -> {
                    if (title != null) {
                        existing.setTitle(title);
                    }
                    if (durationSeconds != null) {
                        existing.setDurationSeconds(durationSeconds);
                    }
                    if (albumId != null) {
                        albumRepository.findById(albumId).ifPresent(existing::setAlbum);
                    }
                    return trackRepository.save(existing);
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        return trackRepository.findById(id)
                .map(t -> {
                    t.getUsers().forEach(user -> user.getTracks().remove(t));
                    trackRepository.delete(t);
                    return true;
                })
                .orElse(false);
    }
}
