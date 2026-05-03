package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.dto.response.UserResponse;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.entity.User;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.UserMapper;
import by.aleksandr.music.repository.TrackRepository;
import by.aleksandr.music.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public PagedResponse<UserResponse> findPage(String name, Pageable pageable) {
        Page<User> page = (name == null || name.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByNameContainingIgnoreCase(name, pageable);

        return new PagedResponse<>(
                page.getContent().stream().map(UserMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
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
                    existing.setName(name);
                    existing.getTracks().clear();
                    existing.getTracks().addAll(resolveTracks(trackIds));
                    User saved = userRepository.save(existing);
                    albumSearchCache.invalidateAll();
                    return saved;
                });
    }

    @Transactional
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        userRepository.delete(user);
        albumSearchCache.invalidateAll();
    }

    private List<Track> resolveTracks(List<Long> trackIds) {
        if (trackIds == null || trackIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Track> tracks = new ArrayList<>(trackRepository.findAllById(trackIds));
        if (tracks.size() != new HashSet<>(trackIds).size()) {
            throw new BadRequestException("One or more track ids do not exist");
        }
        return tracks;
    }
}
