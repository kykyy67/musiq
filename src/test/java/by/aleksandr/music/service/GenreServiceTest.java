package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.entity.Genre;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.repository.GenreRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private AlbumSearchCache albumSearchCache;

    @InjectMocks
    private GenreService genreService;

    @Test
    void findMethodsShouldDelegateToRepository() {
        Genre genre = Genre.builder().id(1L).name("Rock").build();
        when(genreRepository.findAll()).thenReturn(List.of(genre));
        when(genreRepository.findByNameContainingIgnoreCase("ro")).thenReturn(List.of(genre));
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        assertThat(genreService.findAll()).containsExactly(genre);
        assertThat(genreService.findByName("")).containsExactly(genre);
        assertThat(genreService.findByName(null)).containsExactly(genre);
        assertThat(genreService.findByName("ro")).containsExactly(genre);
        assertThat(genreService.findById(1L)).contains(genre);
    }

    @Test
    void createAndUpdateShouldInvalidateCache() {
        Genre existing = Genre.builder().id(1L).name("Old").build();
        Genre request = Genre.builder().name("New").build();
        when(genreRepository.save(existing)).thenReturn(existing);
        when(genreRepository.save(request)).thenReturn(request);
        when(genreRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThat(genreService.create(request)).isSameAs(request);
        assertThat(genreService.update(1L, request)).contains(existing);
        assertThat(existing.getName()).isEqualTo("New");
        verify(albumSearchCache, times(2)).invalidateAll();
    }

    @Test
    void updateShouldReturnEmptyWhenGenreIsMissing() {
        when(genreRepository.findById(8L)).thenReturn(Optional.empty());

        assertThat(genreService.update(8L, Genre.builder().name("Jazz").build())).isEmpty();
    }

    @Test
    void deleteByIdShouldDeleteWhenGenreExists() {
        when(genreRepository.existsById(5L)).thenReturn(true);

        genreService.deleteById(5L);

        verify(genreRepository).deleteById(5L);
        verify(albumSearchCache).invalidateAll();
    }

    @Test
    void deleteByIdShouldThrowWhenGenreDoesNotExist() {
        when(genreRepository.existsById(6L)).thenReturn(false);

        assertThatThrownBy(() -> genreService.deleteById(6L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Genre with id 6 not found");
    }
}
