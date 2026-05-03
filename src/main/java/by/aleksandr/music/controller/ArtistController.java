package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.ArtistRequest;
import by.aleksandr.music.dto.request.ArtistWithAlbumAndTracksRequest;
import by.aleksandr.music.dto.response.ArtistResponse;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.ArtistMapper;
import by.aleksandr.music.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @Operation(summary = "Get artists")
    @GetMapping
    public PagedResponse<ArtistResponse> getAll(
            @RequestParam(name = "name", required = false) String name,
            Pageable pageable) {
        return artistService.findPage(name, pageable);
    }

    @Operation(summary = "Get artist by id")
    @GetMapping("/{id}")
    public ArtistResponse getById(@PathVariable Long id) {
        return artistService.findById(id)
                .map(ArtistMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with id " + id + " not found"));
    }

    @Operation(summary = "Create artist")
    @PostMapping
    public ResponseEntity<ArtistResponse> create(@Valid @RequestBody ArtistRequest request) {
        ArtistResponse created = ArtistMapper.toResponse(
                artistService.create(ArtistMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update artist")
    @PutMapping("/{id}")
    public ArtistResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ArtistRequest request) {
        return artistService.update(id, ArtistMapper.toEntity(request))
                .map(ArtistMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with id " + id + " not found"));
    }

    @Operation(summary = "Delete artist")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create artist, album and tracks without transaction")
    @PostMapping("/composite/without-transaction")
    public ResponseEntity<String> saveCompositeWithoutTransaction(
            @Valid @RequestBody ArtistWithAlbumAndTracksRequest request,
            @RequestParam(name = "fail", defaultValue = "false") boolean fail) {
        artistService.saveArtistWithAlbumAndTracksWithoutTransaction(request, fail);
        return ResponseEntity.ok("Saved without transaction.");
    }

    @Operation(summary = "Create artist, album and tracks with transaction")
    @PostMapping("/composite/with-transaction")
    public ResponseEntity<String> saveCompositeWithTransaction(
            @Valid @RequestBody ArtistWithAlbumAndTracksRequest request,
            @RequestParam(name = "fail", defaultValue = "false") boolean fail) {
        artistService.saveArtistWithAlbumAndTracksWithTransaction(request, fail);
        return ResponseEntity.ok("Saved with transaction.");
    }
}
