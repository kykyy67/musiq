package by.aleksandr.music.repository;

import by.aleksandr.music.entity.User;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"tracks"})
    List<User> findAll();

    @EntityGraph(attributePaths = {"tracks"})
    List<User> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"tracks"})
    Optional<User> findById(Long id);
}
