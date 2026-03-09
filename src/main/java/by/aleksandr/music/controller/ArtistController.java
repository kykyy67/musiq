package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.ArtistRequest;
import by.aleksandr.music.dto.request.ArtistWithAlbumAndTracksRequest;
import by.aleksandr.music.dto.response.ArtistResponse;
import by.aleksandr.music.mapper.ArtistMapper;
import by.aleksandr.music.service.ArtistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public List<ArtistResponse> getAll(
        @RequestParam(name = "name", required = false) String name) {
        return ArtistMapper.toResponseList(artistService.findByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getById(@PathVariable Long id) {
        return artistService.findById(id)
        .map(ArtistMapper::toResponse)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ArtistResponse> create(@RequestBody ArtistRequest request) {
        ArtistResponse created = ArtistMapper.toResponse(
                artistService.create(ArtistMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtistResponse> update(
            @PathVariable Long id,
            @RequestBody ArtistRequest request) {
        return artistService.update(id, ArtistMapper.toEntity(request))
        .map(ArtistMapper::toResponse)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return artistService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/composite/without-transaction")
    public ResponseEntity<String> saveCompositeWithoutTransaction(
            @RequestBody ArtistWithAlbumAndTracksRequest request,
            @RequestParam(name = "fail", defaultValue = "false") boolean fail) { // Добавили name = "fail"
        artistService.saveArtistWithAlbumAndTracksWithoutTransaction(request, fail);
        return ResponseEntity.ok("Сохранено без транзакции.");
    }

    @PostMapping("/composite/with-transaction")
    public ResponseEntity<String> saveCompositeWithTransaction(
            @RequestBody ArtistWithAlbumAndTracksRequest request,
            @RequestParam(name = "fail", defaultValue = "false") boolean fail) { // Добавили name = "fail"
        artistService.saveArtistWithAlbumAndTracksWithTransaction(request, fail);
        return ResponseEntity.ok("Сохранено с транзакцией.");
    }
}

