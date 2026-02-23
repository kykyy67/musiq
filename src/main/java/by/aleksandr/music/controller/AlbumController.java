package by.aleksandr.music.controller;

import by.aleksandr.music.dto.AlbumDto;
import by.aleksandr.music.mapper.AlbumMapper;
import by.aleksandr.music.service.AlbumService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

  private final AlbumService albumService;
  @GetMapping
  public List<AlbumDto> getAlbums(@RequestParam(name = "title", required = false) String title) {
    return AlbumMapper.toDtoList(albumService.getAlbumsByTitle(title));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AlbumDto> getAlbumById(@PathVariable("id") Long id) {
    return albumService.getAlbumById(id)
        .map(AlbumMapper::toDto)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}

