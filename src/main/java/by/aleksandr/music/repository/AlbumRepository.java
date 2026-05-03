package by.aleksandr.music.repository;

import by.aleksandr.music.entity.Album;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @EntityGraph(attributePaths = {"artists", "genres", "tracks"})
    List<Album> findAll();

    @EntityGraph(attributePaths = {"artists", "genres", "tracks"})
    List<Album> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = {"artists", "genres", "tracks"})
    Page<Album> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = {"artists", "genres", "tracks"})
    Page<Album> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"artists", "genres", "tracks"})
    Optional<Album> findWithEntityGraphById(Long id);

    @EntityGraph(attributePaths = {"tracks", "tracks.users", "tracks.users.tracks"})
    Optional<Album> findForDeleteById(Long id);

    @EntityGraph(attributePaths = {"artists", "genres", "tracks"})
    List<Album> findByIdIn(List<Long> ids);

    @Query(
            value = """
                    SELECT DISTINCT a.id
                    FROM Album a
                    LEFT JOIN a.genres g
                    LEFT JOIN a.tracks t
                    WHERE (:genreNameLower IS NULL OR (g IS NOT NULL AND LOWER(g.name) = :genreNameLower))
                      AND (:trackPatternLower IS NULL OR (t IS NOT NULL AND LOWER(t.title) LIKE :trackPatternLower))
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT a.id)
                    FROM Album a
                    LEFT JOIN a.genres g
                    LEFT JOIN a.tracks t
                    WHERE (:genreNameLower IS NULL OR (g IS NOT NULL AND LOWER(g.name) = :genreNameLower))
                      AND (:trackPatternLower IS NULL OR (t IS NOT NULL AND LOWER(t.title) LIKE :trackPatternLower))
                    """
    )
    Page<Long> searchAlbumIdsJpql(
            @Param("genreNameLower") String genreNameLower,
            @Param("trackPatternLower") String trackPatternLower,
            Pageable pageable);

    @Query(
            value = """
                    SELECT DISTINCT a.id
                    FROM albums a
                    LEFT JOIN album_genres ag ON ag.album_id = a.id
                    LEFT JOIN genres g ON g.id = ag.genre_id
                    LEFT JOIN tracks t ON t.album_id = a.id
                    WHERE (:genreNameLower IS NULL OR (g.id IS NOT NULL AND LOWER(g.name) = :genreNameLower))
                      AND (:trackPatternLower IS NULL OR (t.id IS NOT NULL AND LOWER(t.title) LIKE :trackPatternLower))
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT a.id)
                    FROM albums a
                    LEFT JOIN album_genres ag ON ag.album_id = a.id
                    LEFT JOIN genres g ON g.id = ag.genre_id
                    LEFT JOIN tracks t ON t.album_id = a.id
                    WHERE (:genreNameLower IS NULL OR (g.id IS NOT NULL AND LOWER(g.name) = :genreNameLower))
                      AND (:trackPatternLower IS NULL OR (t.id IS NOT NULL AND LOWER(t.title) LIKE :trackPatternLower))
            """,
            nativeQuery = true
    )
    Page<Long> searchAlbumIdsNative(
            @Param("genreNameLower") String genreNameLower,
            @Param("trackPatternLower") String trackPatternLower,
            Pageable pageable);
}
