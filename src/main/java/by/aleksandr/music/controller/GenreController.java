package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.GenreRequest;
import by.aleksandr.music.dto.response.GenreResponse;
import by.aleksandr.music.mapper.GenreMapper;
import by.aleksandr.music.service.GenreService;
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
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public List<GenreResponse> getAll(
            @RequestParam(name = "name", required = false) String name) {
        return GenreMapper.toResponseList(genreService.findByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getById(@PathVariable Long id) {
        return genreService.findById(id)
                .map(GenreMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GenreResponse> create(@RequestBody GenreRequest request) {
        GenreResponse created = GenreMapper.toResponse(
                genreService.create(GenreMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> update(
            @PathVariable Long id,
            @RequestBody GenreRequest request) {
        return genreService.update(id, GenreMapper.toEntity(request))
                .map(GenreMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return genreService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
