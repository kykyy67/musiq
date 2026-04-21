package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.entity.User;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.repository.TrackRepository;
import by.aleksandr.music.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private AlbumSearchCache albumSearchCache;

    @InjectMocks
    private UserService userService;

    @Test
    void findMethodsShouldDelegateToRepository() {
        User user = User.builder().id(1L).name("Ann").build();
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.findByNameContainingIgnoreCase("an")).thenReturn(List.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.findAll()).containsExactly(user);
        assertThat(userService.findByName("")).containsExactly(user);
        assertThat(userService.findByName(null)).containsExactly(user);
        assertThat(userService.findByName("an")).containsExactly(user);
        assertThat(userService.findById(1L)).contains(user);
    }

    @Test
    void createShouldResolveTracksAndInvalidateCache() {
        Track track = Track.builder().id(3L).title("Hysteria").build();
        when(trackRepository.findAllById(List.of(3L))).thenReturn(List.of(track));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.create("Ann", List.of(3L));

        assertThat(created.getTracks()).containsExactly(track);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void createShouldAllowEmptyTrackIds() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.create("Ann", null);

        assertThat(created.getTracks()).isEmpty();
    }

    @Test
    void createShouldThrowWhenTrackIdsAreMissing() {
        when(trackRepository.findAllById(List.of(3L, 4L))).thenReturn(List.of(Track.builder().id(3L).build()));
        List<Long> trackIds = List.of(3L, 4L);

        assertThatThrownBy(() -> userService.create("Ann", trackIds))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("One or more track ids do not exist");
    }

    @Test
    void updateShouldReturnUpdatedUserWhenFound() {
        User existing = User.builder().id(1L).name("Old").tracks(new java.util.ArrayList<>()).build();
        Track track = Track.builder().id(5L).title("Map of the Problematique").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trackRepository.findAllById(List.of(5L))).thenReturn(List.of(track));
        when(userRepository.save(existing)).thenReturn(existing);

        Optional<User> updated = userService.update(1L, "New", List.of(5L));

        assertThat(updated).contains(existing);
        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getTracks()).containsExactly(track);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void updateShouldClearTracksWhenEmptyListIsProvided() {
        User existing = User.builder()
                .id(1L)
                .name("Old")
                .tracks(new java.util.ArrayList<>(List.of(Track.builder().id(5L).build())))
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        Optional<User> updated = userService.update(1L, "New", List.of());

        assertThat(updated).contains(existing);
        assertThat(existing.getTracks()).isEmpty();
    }

    @Test
    void updateShouldReturnEmptyWhenUserDoesNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(userService.update(2L, "Name", List.of())).isEmpty();
    }

    @Test
    void deleteByIdShouldDeleteUserAndInvalidateCache() {
        User user = User.builder().id(7L).name("Ann").build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        userService.deleteById(7L);

        verify(userRepository).delete(user);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void deleteByIdShouldThrowWhenUserIsMissing() {
        when(userRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteById(8L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id 8 not found");
    }
}
