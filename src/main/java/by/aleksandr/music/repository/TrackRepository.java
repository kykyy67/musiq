package by.aleksandr.music.repository;

import by.aleksandr.music.entity.Track;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {

    //@EntityGraph(attributePaths = {"album", "album.artist"})
    List<Track> findAll();

    @EntityGraph(attributePaths = {"album", "album.artist"})
    List<Track> findByTitleContainingIgnoreCase(String title);
}
