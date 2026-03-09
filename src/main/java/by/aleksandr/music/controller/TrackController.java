package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.TrackRequest;
import by.aleksandr.music.dto.response.TrackResponse;
import by.aleksandr.music.mapper.TrackMapper;
import by.aleksandr.music.service.TrackService;
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
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @GetMapping
    public List<TrackResponse> getAll(
            @RequestParam(name = "title", required = false) String title) {
        return TrackMapper.toResponseList(trackService.findByTitle(title));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackResponse> getById(@PathVariable Long id) {
        return trackService.findById(id)
                .map(TrackMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TrackResponse> create(@RequestBody TrackRequest request) {
        return trackService.create(
                request.getTitle(),
                        request.getDurationSeconds(),
                        request.getAlbumId())
                .map(TrackMapper::toResponse)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackResponse> update(
            @PathVariable Long id,
            @RequestBody TrackRequest request) {
        return trackService.update(
                id,
                        request.getTitle(),
                        request.getDurationSeconds(),
                        request.getAlbumId())
                .map(TrackMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return trackService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
