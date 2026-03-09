package by.aleksandr.music.service;

import by.aleksandr.music.dto.request.ArtistWithAlbumAndTracksRequest;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.entity.Artist;
import by.aleksandr.music.entity.Track;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.ArtistRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    public List<Artist> findAll() {
        return artistRepository.findAll();
    }

    public List<Artist> findByName(String name) {
        if (name == null || name.isBlank()) {
            return findAll();
        }
        return artistRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<Artist> findById(Long id) {
        return artistRepository.findById(id);
    }

    @Transactional
    public Artist create(Artist artist) {
        return artistRepository.save(artist);
    }

    @Transactional
    public Optional<Artist> update(Long id, Artist artist) {
        return artistRepository.findById(id)
                .map(existing -> {
                    existing.setName(artist.getName());
                    return artistRepository.save(existing);
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        return artistRepository.findById(id)
                .map(a -> {
                    artistRepository.delete(a);
                    return true;
                })
                .orElse(false);
    }

    public void saveArtistWithAlbumAndTracksWithoutTransaction(
            ArtistWithAlbumAndTracksRequest request,
            boolean simulateFailureAfterAlbum) {

        Artist artist = Artist.builder().name(request.getArtistName()).build();
        artist = artistRepository.save(artist);

        Album album = Album.builder()
                .title(request.getAlbumTitle())
                .releaseYear(request.getReleaseYear())
                .artist(artist)
                .build();
        album = albumRepository.save(album);

        if (simulateFailureAfterAlbum) {
            throw new IllegalArgumentException("ОШИБОЧКА");
        }

        if (request.getTracks() != null) {
            for (ArtistWithAlbumAndTracksRequest.TrackItem item : request.getTracks()) {
                Track track = Track.builder()
                        .title(item.getTitle())
                        .durationSeconds(item.getDurationSeconds() != null ? item.getDurationSeconds() : 0)
                        .album(album)
                        .build();
                trackRepository.save(track);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveArtistWithAlbumAndTracksWithTransaction(
            ArtistWithAlbumAndTracksRequest request,
            boolean simulateFailureAfterAlbum) {

        Artist artist = Artist.builder().name(request.getArtistName()).build();
        artist = artistRepository.save(artist);

        Album album = Album.builder()
                .title(request.getAlbumTitle())
                .releaseYear(request.getReleaseYear())
                .artist(artist)
                .build();
        album = albumRepository.save(album);

        if (simulateFailureAfterAlbum) {
            throw new IllegalArgumentException("ОШибочка!-_-");
        }

        if (request.getTracks() != null) {
            for (ArtistWithAlbumAndTracksRequest.TrackItem item : request.getTracks()) {
                Track track = Track.builder()
                        .title(item.getTitle())
                        .durationSeconds(item.getDurationSeconds() != null ? item.getDurationSeconds() : 0)
                        .album(album)
                        .build();
                trackRepository.save(track);
            }
        }
    }
}

