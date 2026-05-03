package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.BulkTrackItemRequest;
import by.aleksandr.music.dto.request.TrackRequest;
import by.aleksandr.music.dto.response.PagedResponse;
import by.aleksandr.music.dto.response.TrackResponse;
import by.aleksandr.music.exception.ResourceNotFoundException;
import by.aleksandr.music.mapper.TrackMapper;
import by.aleksandr.music.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @Operation(summary = "Get tracks")
    @GetMapping
    public PagedResponse<TrackResponse> getAll(
            @RequestParam(name = "title", required = false) String title,
            Pageable pageable) {
        return trackService.findPage(title, pageable);
    }

    @Operation(summary = "Get track by id")
    @GetMapping("/{id}")
    public TrackResponse getById(@PathVariable Long id) {
        return trackService.findById(id)
                .map(TrackMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Track with id " + id + " not found"));
    }

    @Operation(summary = "Create track")
    @PostMapping
    public ResponseEntity<TrackResponse> create(@Valid @RequestBody TrackRequest request) {
        TrackResponse response = TrackMapper.toResponse(
                trackService.create(
                        request.getTitle(),
                        request.getDurationSeconds(),
                        request.getAlbumId(),
                        request.getArtistId(),
                        request.getGenreId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Bulk create tracks without transaction")
    @PostMapping("/bulk/without-transaction")
    public ResponseEntity<List<TrackResponse>> createBulkWithoutTransaction(
            @RequestParam Long albumId,
            @RequestParam(name = "fail", required = false) Integer fail,
            @RequestBody @NotEmpty List<@Valid BulkTrackItemRequest> requests) {
        List<TrackResponse> response = trackService.createBulkWithoutTransaction(albumId, requests, fail)
                .stream()
                .map(TrackMapper::toResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Bulk create tracks with transaction")
    @PostMapping("/bulk/with-transaction")
    public ResponseEntity<List<TrackResponse>> createBulkWithTransaction(
            @RequestParam Long albumId,
            @RequestParam(name = "fail", required = false) Integer fail,
            @RequestBody @NotEmpty List<@Valid BulkTrackItemRequest> requests) {
        List<TrackResponse> response = trackService.createBulkWithTransaction(albumId, requests, fail)
                .stream()
                .map(TrackMapper::toResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update track")
    @PutMapping("/{id}")
    public TrackResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TrackRequest request) {
        return TrackMapper.toResponse(
                trackService.update(
                        id,
                        request.getTitle(),
                        request.getDurationSeconds(),
                        request.getAlbumId(),
                        request.getArtistId(),
                        request.getGenreId()));
    }

    @Operation(summary = "Delete track")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trackService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
