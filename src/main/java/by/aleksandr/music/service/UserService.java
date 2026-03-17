package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.entity.User;
import by.aleksandr.music.repository.TrackRepository;
import by.aleksandr.music.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final AlbumSearchCache albumSearchCache;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByName(String name) {
        if (name == null || name.isBlank()) {
            return findAll();
        }
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User create(String name, List<Long> trackIds) {
        User user = User.builder()
                .name(name)
                .tracks(resolveTracks(trackIds))
                .build();
        User saved = userRepository.save(user);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public Optional<User> update(Long id, String name, List<Long> trackIds) {
        return userRepository.findById(id)
                .map(existing -> {
                    if (name != null) {
                        existing.setName(name);
                    }
                    if (trackIds != null) {
                        existing.getTracks().clear();
                        existing.getTracks().addAll(resolveTracks(trackIds));
                    }
                    User saved = userRepository.save(existing);
                    albumSearchCache.invalidateAll();
                    return saved;
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        return userRepository.findById(id)
                .map(u -> {
                    userRepository.delete(u);
                    albumSearchCache.invalidateAll();
                    return true;
                })
                .orElse(false);
    }

    private List<Track> resolveTracks(List<Long> trackIds) {
        if (trackIds == null || trackIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Track> tracks = new ArrayList<>();
        for (Long trackId : trackIds) {
            trackRepository.findById(trackId).ifPresent(tracks::add);
        }
        return tracks;
    }
}
