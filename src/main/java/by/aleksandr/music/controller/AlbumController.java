package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.AlbumRequest;
import by.aleksandr.music.dto.response.AlbumResponse;
import by.aleksandr.music.mapper.AlbumMapper;
import by.aleksandr.music.service.AlbumService;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public List<AlbumResponse> getAlbums(
            @RequestParam(name = "title", required = false) String title) {
        return AlbumMapper.toResponseList(albumService.getAlbumsByTitleWithArtistAndGenres(title));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbumById(@PathVariable Long id) {
        return albumService.getAlbumById(id)
            .map(AlbumMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> create(@RequestBody AlbumRequest request) {
        return albumService.create(request)
            .map(AlbumMapper::toResponse)
            .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")

    public ResponseEntity<AlbumResponse> update(
          @PathVariable Long id,
          @RequestBody AlbumRequest request) {
        return albumService.update(id, request)
            .map(AlbumMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return albumService.deleteById(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
