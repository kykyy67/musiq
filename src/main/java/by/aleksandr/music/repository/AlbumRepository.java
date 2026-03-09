package by.aleksandr.music.repository;

import by.aleksandr.music.entity.Album;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AlbumRepository extends JpaRepository<Album, Long> {

    @EntityGraph(attributePaths = {"artist", "genres", "tracks"})
    List<Album> findAll();

    @EntityGraph(attributePaths = {"artist", "genres", "tracks"})
    List<Album> findByTitleContainingIgnoreCase(String title);
}