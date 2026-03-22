package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.GenreRequest;
import by.aleksandr.music.dto.response.GenreResponse;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.GenreMapper;
import by.aleksandr.music.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @Operation(summary = "Get genres")
    @GetMapping
    public List<GenreResponse> getAll(
            @RequestParam(name = "name", required = false) String name) {
        return GenreMapper.toResponseList(genreService.findByName(name));
    }

    @Operation(summary = "Get genre by id")
    @GetMapping("/{id}")
    public GenreResponse getById(@PathVariable Long id) {
        return genreService.findById(id)
                .map(GenreMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Genre with id " + id + " not found"));
    }

    @Operation(summary = "Create genre")
    @PostMapping
    public ResponseEntity<GenreResponse> create(@Valid @RequestBody GenreRequest request) {
        GenreResponse created = GenreMapper.toResponse(
                genreService.create(GenreMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update genre")
    @PutMapping("/{id}")
    public GenreResponse update(
            @PathVariable Long id,
            @Valid @RequestBody GenreRequest request) {
        return genreService.update(id, GenreMapper.toEntity(request))
                .map(GenreMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Genre with id " + id + " not found"));
    }

    @Operation(summary = "Delete genre")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        genreService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
