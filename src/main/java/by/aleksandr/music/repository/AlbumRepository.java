package by.aleksandr.music.repository;

import by.aleksandr.music.entity.Album;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {

  List<Album> findByTitleContainingIgnoreCase(String title);
}
