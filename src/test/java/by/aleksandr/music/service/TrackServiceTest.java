package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.BulkTrackItemRequest;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.entity.User;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private AlbumSearchCache albumSearchCache;

    @InjectMocks
    private TrackService trackService;

    @Test
    void findMethodsShouldDelegateToRepository() {
        Track track = Track.builder().id(1L).title("Hysteria").build();
        when(trackRepository.findAll()).thenReturn(List.of(track));
        when(trackRepository.findByTitleContainingIgnoreCase("hys")).thenReturn(List.of(track));
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        assertThat(trackService.findAll()).containsExactly(track);
        assertThat(trackService.findByTitle("")).containsExactly(track);
        assertThat(trackService.findByTitle(null)).containsExactly(track);
        assertThat(trackService.findByTitle("hys")).containsExactly(track);
        assertThat(trackService.findById(1L)).contains(track);
    }

    @Test
    void createShouldPersistTrackForAlbum() {
        Album album = Album.builder().id(7L).title("Absolution").build();
        when(albumRepository.findById(7L)).thenReturn(Optional.of(album));
        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Track created = trackService.create("Hysteria", 227, 7L, null, null);

        assertThat(created.getAlbum()).isSameAs(album);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void createShouldThrowWhenAlbumDoesNotExist() {
        when(albumRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackService.create("Hysteria", 227, 7L, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album with id 7 not found");
    }

    @Test
    void updateShouldPersistChangesWhenTrackAndAlbumExist() {
        Track existing = Track.builder().id(9L).title("Old").durationSeconds(100).build();
        Album album = Album.builder().id(7L).title("Absolution").build();
        when(trackRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(albumRepository.findById(7L)).thenReturn(Optional.of(album));
        when(trackRepository.save(existing)).thenReturn(existing);

        Track updated = trackService.update(9L, "Hysteria", 227, 7L, null, null);

        assertThat(updated.getTitle()).isEqualTo("Hysteria");
        assertThat(updated.getDurationSeconds()).isEqualTo(227);
        assertThat(updated.getAlbum()).isSameAs(album);
    }

    @Test
    void updateShouldThrowWhenTrackDoesNotExist() {
        when(trackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackService.update(99L, "Hysteria", 227, 7L, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Track with id 99 not found");
    }

    @Test
    void updateShouldThrowWhenAlbumDoesNotExist() {
        Track existing = Track.builder().id(9L).title("Old").durationSeconds(100).build();
        when(trackRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(albumRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackService.update(9L, "Hysteria", 227, 7L, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album with id 7 not found");
    }

    @Test
    void deleteByIdShouldUnlinkUsersDeleteTrackAndInvalidateCache() {
        Track track = Track.builder().id(3L).title("Hysteria").users(new ArrayList<>()).build();
        User user = User.builder().id(4L).name("Ann").tracks(new ArrayList<>(List.of(track))).build();
        track.getUsers().add(user);
        when(trackRepository.findById(3L)).thenReturn(Optional.of(track));

        trackService.deleteById(3L);

        assertThat(user.getTracks()).isEmpty();
        verify(trackRepository).delete(track);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void deleteByIdShouldThrowWhenTrackDoesNotExist() {
        when(trackRepository.findById(13L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackService.deleteById(13L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Track with id 13 not found");
    }

    @Test
    void createBulkWithoutTransactionShouldSaveAllTracksUsingStreamPipeline() {
        Album album = Album.builder().id(1L).title("Absolution").build();
        AtomicLong ids = new AtomicLong(0);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> {
            Track track = invocation.getArgument(0);
            track.setId(ids.incrementAndGet());
            return track;
        });

        List<Track> created = trackService.createBulkWithoutTransaction(
                1L,
                List.of(
                        new BulkTrackItemRequest("Hysteria", 227),
                        new BulkTrackItemRequest("Stockholm Syndrome", 290)),
                null);

        assertThat(created).extracting(Track::getTitle).containsExactly("Hysteria", "Stockholm Syndrome");
        verify(trackRepository, times(2)).save(any(Track.class));
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void createBulkWithTransactionShouldIgnoreNonMatchingFailIndex() {
        Album album = Album.builder().id(1L).title("Absolution").build();
        AtomicLong ids = new AtomicLong(0);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> {
            Track track = invocation.getArgument(0);
            track.setId(ids.incrementAndGet());
            return track;
        });

        List<Track> created = trackService.createBulkWithTransaction(
                1L,
                List.of(
                        new BulkTrackItemRequest("Hysteria", 227),
                        new BulkTrackItemRequest("Blackout", 280)),
                99);

        assertThat(created).hasSize(2);
        verify(trackRepository, times(2)).save(any(Track.class));
    }

    @Test
    void createBulkWithTransactionShouldThrowWhenListIsEmpty() {
        assertThatThrownBy(() -> trackService.createBulkWithTransaction(1L, List.of(), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Track list must not be empty");
    }

    @Test
    void createBulkWithTransactionShouldThrowWhenListIsNull() {
        assertThatThrownBy(() -> trackService.createBulkWithTransaction(1L, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Track list must not be empty");
    }

    @Test
    void createBulkWithoutTransactionShouldStopAtConfiguredFailureIndex() {
        Album album = Album.builder().id(1L).title("Absolution").build();
        List<BulkTrackItemRequest> requests = List.of(
                new BulkTrackItemRequest("Hysteria", 227),
                new BulkTrackItemRequest("Blackout", 280));
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> trackService.createBulkWithoutTransaction(1L, requests, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Simulated bulk failure at index 1");

        verify(trackRepository, times(1)).save(any(Track.class));
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void createBulkWithTransactionShouldThrowWhenAlbumDoesNotExist() {
        when(albumRepository.findById(8L)).thenReturn(Optional.empty());
        List<BulkTrackItemRequest> requests = List.of(new BulkTrackItemRequest("Hysteria", 227));
        Long albumId = 8L;

        assertThatThrownBy(() -> trackService.createBulkWithTransaction(albumId, requests, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album with id 8 not found");
    }
}
