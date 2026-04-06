package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.ArtistWithAlbumAndTracksRequest;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Artist;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.ArtistRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private AlbumSearchCache albumSearchCache;

    @InjectMocks
    private ArtistService artistService;

    @Test
    void findMethodsShouldDelegateToRepository() {
        Artist artist = Artist.builder().id(1L).name("Muse").build();
        when(artistRepository.findAll()).thenReturn(List.of(artist));
        when(artistRepository.findByNameContainingIgnoreCase("mu")).thenReturn(List.of(artist));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThat(artistService.findAll()).containsExactly(artist);
        assertThat(artistService.findByName("")).containsExactly(artist);
        assertThat(artistService.findByName("mu")).containsExactly(artist);
        assertThat(artistService.findById(1L)).contains(artist);
    }

    @Test
    void createShouldSaveArtistAndInvalidateCache() {
        Artist artist = Artist.builder().name("Muse").build();
        when(artistRepository.save(artist)).thenReturn(artist);

        Artist saved = artistService.create(artist);

        assertThat(saved).isSameAs(artist);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void updateShouldReturnUpdatedArtistWhenFound() {
        Artist existing = Artist.builder().id(1L).name("Old").build();
        Artist request = Artist.builder().name("New").build();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(artistRepository.save(existing)).thenReturn(existing);

        Optional<Artist> updated = artistService.update(1L, request);

        assertThat(updated).contains(existing);
        assertThat(existing.getName()).isEqualTo("New");
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void updateShouldReturnEmptyWhenArtistIsMissing() {
        when(artistRepository.findById(5L)).thenReturn(Optional.empty());

        assertThat(artistService.update(5L, Artist.builder().name("New").build())).isEmpty();
    }

    @Test
    void deleteByIdShouldThrowWhenArtistDoesNotExist() {
        when(artistRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.deleteById(3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artist with id 3 not found");
    }

    @Test
    void saveArtistWithAlbumAndTracksWithoutTransactionShouldCreateFullAggregate() {
        ArtistWithAlbumAndTracksRequest request = request();
        Artist savedArtist = Artist.builder().id(1L).name("Muse").build();
        Album savedAlbum = Album.builder().id(2L).title("Absolution").build();

        when(artistRepository.save(any(Artist.class))).thenReturn(savedArtist);
        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

        artistService.saveArtistWithAlbumAndTracksWithoutTransaction(request, false);

        verify(trackRepository).save(any(Track.class));
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void saveArtistWithAlbumAndTracksWithTransactionShouldThrowWhenFailureIsSimulated() {
        ArtistWithAlbumAndTracksRequest request = request();
        when(artistRepository.save(any(Artist.class))).thenReturn(Artist.builder().id(1L).name("Muse").build());
        when(albumRepository.save(any(Album.class))).thenReturn(Album.builder().id(2L).title("Absolution").build());

        assertThatThrownBy(() -> artistService.saveArtistWithAlbumAndTracksWithTransaction(request, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Simulated composite save failure");

        verify(trackRepository, never()).save(any(Track.class));
    }

    private ArtistWithAlbumAndTracksRequest request() {
        return new ArtistWithAlbumAndTracksRequest(
                "Muse",
                "Absolution",
                2003,
                List.of(new ArtistWithAlbumAndTracksRequest.TrackItem("Time Is Running Out", 217)));
    }
}
