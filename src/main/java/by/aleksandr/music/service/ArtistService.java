package by.aleksandr.music.service;

import by.aleksandr.music.cache.AlbumSearchCache;
import by.aleksandr.music.dto.request.ArtistWithAlbumAndTracksRequest;
import by.aleksandr.music.dto.response.ArtistResponse;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Artist;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.ArtistMapper;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.ArtistRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final AlbumSearchCache albumSearchCache;

    public List<Artist> findAll() {
        return artistRepository.findAll();
    }

    public List<Artist> findByName(String name) {
        if (name == null || name.isBlank()) {
            return findAll();
        }
        return artistRepository.findByNameContainingIgnoreCase(name);
    }

    public by.aleksandr.music.dto.response.PagedResponse<ArtistResponse> findPage(String name, Pageable pageable) {
        Page<Artist> page = (name == null || name.isBlank())
                ? artistRepository.findAll(pageable)
                : artistRepository.findByNameContainingIgnoreCase(name, pageable);

        return new by.aleksandr.music.dto.response.PagedResponse<>(
                page.getContent().stream().map(ArtistMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public Optional<Artist> findById(Long id) {
        return artistRepository.findById(id);
    }

    @Transactional
    public Artist create(Artist artist) {
        Artist saved = artistRepository.save(artist);
        albumSearchCache.invalidateAll();
        return saved;
    }

    @Transactional
    public Optional<Artist> update(Long id, Artist artist) {
        return artistRepository.findById(id)
                .map(existing -> {
                    existing.setName(artist.getName());
                    Artist saved = artistRepository.save(existing);
                    albumSearchCache.invalidateAll();
                    return saved;
                });
    }

    @Transactional
    public void deleteById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with id " + id + " not found"));
        artistRepository.delete(artist);
        albumSearchCache.invalidateAll();
    }

    public void saveArtistWithAlbumAndTracksWithoutTransaction(
            ArtistWithAlbumAndTracksRequest request,
            boolean simulateFailureAfterAlbum) {
        createArtist(request, simulateFailureAfterAlbum);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveArtistWithAlbumAndTracksWithTransaction(
            ArtistWithAlbumAndTracksRequest request,
            boolean simulateFailureAfterAlbum) {
        createArtist(request, simulateFailureAfterAlbum);
    }

    private Artist createArtist(
            ArtistWithAlbumAndTracksRequest request,
            boolean simulateFailureAfterAlbum) {
        Artist artist = Artist.builder().name(request.getArtistName()).build();
        artist = artistRepository.save(artist);

        Album album = Album.builder()
                .title(request.getAlbumTitle())
                .releaseYear(request.getReleaseYear())
                .artists(Set.of(artist))
                .build();
        album = albumRepository.save(album);

        if (simulateFailureAfterAlbum) {
            throw new BadRequestException("Simulated composite save failure");
        }

        for (ArtistWithAlbumAndTracksRequest.TrackItem item : request.getTracks()) {
            Track track = Track.builder()
                    .title(item.getTitle())
                    .durationSeconds(item.getDurationSeconds())
                    .album(album)
                    .build();
            trackRepository.save(track);
        }
        albumSearchCache.invalidateAll();
        return artist;
    }
}
