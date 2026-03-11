package by.aleksandr.music.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequest {

    private String title;
    private int releaseYear;
    private List<Long> artistIds; // Изменено с Long на List<Long>
    private List<Long> genreIds;
}
