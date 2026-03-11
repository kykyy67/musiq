package by.aleksandr.music.repository;

import by.aleksandr.music.entity.Track;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {

    @EntityGraph(attributePaths = {"album", "album.artists"})
    List<Track> findAll();

    @EntityGraph(attributePaths = {"album", "album.artists"})
    List<Track> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = {"album", "album.artists"})
    Optional<Track> findById(Long id);
}
