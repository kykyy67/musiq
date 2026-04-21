package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import by.aleksandr.music.dto.request.BulkTrackItemRequest;
import by.aleksandr.music.entity.Album;
import by.aleksandr.music.exception.BadRequestException;
import by.aleksandr.music.repository.AlbumRepository;
import by.aleksandr.music.repository.TrackRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:musicdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class TrackServiceTransactionTest {

    @Autowired
    private TrackService trackService;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private TrackRepository trackRepository;

    private Long albumId;

    @BeforeEach
    void setUp() {
        trackRepository.deleteAll();
        albumRepository.deleteAll();
        albumId = albumRepository.save(Album.builder().title("Absolution").releaseYear(2003).build()).getId();
    }

    @Test
    void bulkWithoutTransactionShouldKeepAlreadySavedTracksAfterFailure() {
        List<BulkTrackItemRequest> requests = List.of(
                new BulkTrackItemRequest("Hysteria", 227),
                new BulkTrackItemRequest("Blackout", 280));

        assertThatThrownBy(() -> trackService.createBulkWithoutTransaction(albumId, requests, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Simulated bulk failure at index 1");

        assertThat(trackRepository.findAll()).extracting("title").containsExactly("Hysteria");
    }

    @Test
    void bulkWithTransactionShouldRollbackAllTracksAfterFailure() {
        List<BulkTrackItemRequest> requests = List.of(
                new BulkTrackItemRequest("Hysteria", 227),
                new BulkTrackItemRequest("Blackout", 280));

        assertThatThrownBy(() -> trackService.createBulkWithTransaction(albumId, requests, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Simulated bulk failure at index 1");

        assertThat(trackRepository.findAll()).isEmpty();
    }
}
