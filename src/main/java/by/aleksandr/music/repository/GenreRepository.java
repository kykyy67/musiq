package by.aleksandr.music.repository;

import by.aleksandr.music.entity.Genre;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    @EntityGraph(attributePaths = {"albums"})
    List<Genre> findAll();

    @EntityGraph(attributePaths = {"albums"})
    List<Genre> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"albums"})
    Page<Genre> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"albums"})
    Page<Genre> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"albums"})
    Optional<Genre> findById(Long id);
}
