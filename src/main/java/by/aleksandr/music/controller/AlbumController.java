package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.AlbumRequest;
import by.aleksandr.music.dto.response.AlbumResponse;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.exception.ApiErrorResponse;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.AlbumMapper;
import by.aleksandr.music.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "Get albums", description = "Returns all albums or filters them by title.")
    @GetMapping
    public List<AlbumResponse> getAlbums(
            @RequestParam(name = "title", required = false) String title) {
        return AlbumMapper.toResponseList(albumService.getAlbumsByTitleWithArtistAndGenres(title));
    }

    @Operation(summary = "Search albums", description = "Searches albums by genre and track title with pagination.")
    @GetMapping("/search")
    public PagedResponse<AlbumResponse> search(
            @RequestParam(required = false) String genreName,
            @RequestParam(required = false) String trackTitle,
            @RequestParam(defaultValue = "false") boolean nativeQuery,
            Pageable pageable) {
        return albumService.searchAlbumsByGenreAndTrack(genreName, trackTitle, nativeQuery, pageable);
    }

    @Operation(summary = "Get album by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Album found"),
        @ApiResponse(
            responseCode = "404",
            description = "Album not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    @GetMapping("/{id}")
    public AlbumResponse getAlbumById(@PathVariable Long id) {
        return albumService.getAlbumById(id)
                .map(AlbumMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Album with id " + id + " not found"));
    }

    @Operation(summary = "Create album")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Album created"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    @PostMapping
    public ResponseEntity<AlbumResponse> create(@Valid @RequestBody AlbumRequest request) {
        AlbumResponse response = AlbumMapper.toResponse(albumService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update album")
    @PutMapping("/{id}")
    public AlbumResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AlbumRequest request) {
        return AlbumMapper.toResponse(albumService.update(id, request));
    }

    @Operation(summary = "Delete album")
    @ApiResponse(responseCode = "204", description = "Album deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
