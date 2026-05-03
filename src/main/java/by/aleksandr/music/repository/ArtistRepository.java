package by.aleksandr.music.repository;

import by.aleksandr.music.entity.Artist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    @EntityGraph(attributePaths = {"albums"})
    List<Artist> findAll();

    @EntityGraph(attributePaths = {"albums"})
    List<Artist> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"albums"})
    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"albums"})
    Page<Artist> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"albums"})
    Optional<Artist> findById(Long id);
}
