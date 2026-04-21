package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.AlbumRequest;
import by.aleksandr.music.dto.response.AlbumResponse;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Artist;
import by.aleksandr.music.entity.Genre;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.entity.User;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.ArtistRepository;
import by.aleksandr.music.repository.GenreRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private AlbumSearchCache albumSearchCache;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void getAlbumsVariantsShouldDelegateToRepository() {
        List<Album> albums = List.of(Album.builder().id(1L).title("Origin").build());
        when(albumRepository.findAll()).thenReturn(albums);
        when(albumRepository.findByTitleContainingIgnoreCase("ori")).thenReturn(albums);
        when(albumRepository.findWithEntityGraphById(1L)).thenReturn(Optional.of(albums.getFirst()));

        assertThat(albumService.getAllAlbums()).containsExactlyElementsOf(albums);
        assertThat(albumService.getAlbumsByTitle(null)).containsExactlyElementsOf(albums);
        assertThat(albumService.getAlbumsByTitle(" ")).containsExactlyElementsOf(albums);
        assertThat(albumService.getAlbumsByTitleWithArtistAndGenres("ori")).containsExactlyElementsOf(albums);
        assertThat(albumService.getAlbumById(1L)).contains(albums.getFirst());
    }

    @Test
    void searchAlbumsByGenreAndTrackShouldReturnCachedValueWhenPresent() {
        PageRequest pageable = PageRequest.of(0, 2);
        PagedResponse<AlbumResponse> cached = new PagedResponse<>(List.of(), 0, 2, 0, 0);
        when(albumSearchCache.get(AlbumSearchCache.keyOf("rock", "time", false, pageable)))
                .thenReturn(cached);

        PagedResponse<AlbumResponse> response = albumService.searchAlbumsByGenreAndTrack(
                "rock", "time", false, pageable);

        assertThat(response).isSameAs(cached);
        verify(albumRepository, never()).searchAlbumIdsJpql(any(), any(), any());
    }

    @Test
    void searchAlbumsByGenreAndTrackShouldLoadAndCacheOrderedAlbums() {
        Album first = Album.builder().id(10L).title("First").build();
        Album second = Album.builder().id(20L).title("Second").build();
        PageRequest pageable = PageRequest.of(0, 2);

        when(albumSearchCache.get(any())).thenReturn(null);
        when(albumRepository.searchAlbumIdsJpql("rock", "%time%", pageable))
                .thenReturn(new PageImpl<>(List.of(20L, 10L), pageable, 2));
        when(albumRepository.findByIdIn(List.of(20L, 10L))).thenReturn(List.of(first, second));

        PagedResponse<AlbumResponse> response = albumService.searchAlbumsByGenreAndTrack(
                " ROCK ", " Time ", false, pageable);

        assertThat(response.getContent()).extracting("id").containsExactly(20L, 10L);
        verify(albumSearchCache).put(any(), eq(response));
    }

    @Test
    void searchAlbumsByGenreAndTrackShouldUseNativeQueryAndHandleEmptyPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(albumSearchCache.get(any())).thenReturn(null);
        when(albumRepository.searchAlbumIdsNative(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PagedResponse<AlbumResponse> response = albumService.searchAlbumsByGenreAndTrack(" ", null, true, pageable);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    void createShouldResolveRelationsAndInvalidateCache() {
        AlbumRequest request = new AlbumRequest("Black Holes", 2006, List.of(1L), List.of(2L));
        Artist artist = Artist.builder().id(1L).name("Muse").build();
        Genre genre = Genre.builder().id(2L).name("Rock").build();
        when(artistRepository.findAllById(List.of(1L))).thenReturn(List.of(artist));
        when(genreRepository.findAllById(List.of(2L))).thenReturn(List.of(genre));
        when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Album saved = albumService.create(request);

        assertThat(saved.getArtists()).containsExactly(artist);
        assertThat(saved.getGenres()).containsExactly(genre);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void createShouldAllowMissingOptionalRelations() {
        AlbumRequest request = new AlbumRequest("Black Holes", 2006, null, List.of());
        when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Album saved = albumService.create(request);

        assertThat(saved.getArtists()).isEmpty();
        assertThat(saved.getGenres()).isEmpty();
    }

    @Test
    void createShouldAllowEmptyArtistIdsAndNullGenreIds() {
        AlbumRequest request = new AlbumRequest("Black Holes", 2006, List.of(), null);
        when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Album saved = albumService.create(request);

        assertThat(saved.getArtists()).isEmpty();
        assertThat(saved.getGenres()).isEmpty();
    }

    @Test
    void createShouldThrowWhenArtistIdsAreMissing() {
        AlbumRequest request = new AlbumRequest("Black Holes", 2006, List.of(1L, 2L), List.of());
        when(artistRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(Artist.builder().id(1L).build()));

        assertThatThrownBy(() -> albumService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("One or more artist ids do not exist");
    }

    @Test
    void createShouldThrowWhenGenreIdsAreMissing() {
        AlbumRequest request = new AlbumRequest("Black Holes", 2006, List.of(), List.of(1L, 2L));
        when(genreRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(Genre.builder().id(1L).build()));

        assertThatThrownBy(() -> albumService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("One or more genre ids do not exist");
    }

    @Test
    void updateShouldReplaceRelationsAndInvalidateCache() {
        Album existing = Album.builder()
                .id(5L)
                .title("Old")
                .releaseYear(1999)
                .artists(new java.util.HashSet<>(Set.of(Artist.builder().id(7L).name("Old Artist").build())))
                .genres(new java.util.HashSet<>(Set.of(Genre.builder().id(8L).name("Old Genre").build())))
                .build();
        AlbumRequest request = new AlbumRequest("New", 2001, List.of(1L), List.of(2L));
        Artist artist = Artist.builder().id(1L).name("New Artist").build();
        Genre genre = Genre.builder().id(2L).name("New Genre").build();

        when(albumRepository.findWithEntityGraphById(5L)).thenReturn(Optional.of(existing));
        when(artistRepository.findAllById(List.of(1L))).thenReturn(List.of(artist));
        when(genreRepository.findAllById(List.of(2L))).thenReturn(List.of(genre));
        when(albumRepository.save(existing)).thenReturn(existing);

        Album updated = albumService.update(5L, request);

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getArtists()).containsExactly(artist);
        assertThat(updated.getGenres()).containsExactly(genre);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void updateShouldThrowWhenAlbumDoesNotExist() {
        when(albumRepository.findWithEntityGraphById(99L)).thenReturn(Optional.empty());
        AlbumRequest request = new AlbumRequest("New", 2001, List.of(), List.of());

        assertThatThrownBy(() -> albumService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album with id 99 not found");
    }

    @Test
    void deleteByIdShouldUnlinkUsersDeleteAlbumAndInvalidateCache() {
        Track track = Track.builder().id(3L).title("Plug In Baby").build();
        User user = User.builder().id(4L).name("Ann").tracks(new java.util.ArrayList<>(List.of(track))).build();
        track.setUsers(List.of(user));
        Album album = Album.builder().id(5L).title("Origin").tracks(Set.of(track)).build();
        when(albumRepository.findForDeleteById(5L)).thenReturn(Optional.of(album));

        albumService.deleteById(5L);

        assertThat(user.getTracks()).isEmpty();
        verify(albumRepository).delete(album);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void deleteByIdShouldThrowWhenAlbumDoesNotExist() {
        when(albumRepository.findForDeleteById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.deleteById(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album with id 77 not found");
    }
}
