package by.aleksandr.music.cache;

import by.aleksandr.music.dto.response.AlbumResponse;
import by.aleksandr.music.dto.response.PagedResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class AlbumSearchCache {

    private final Map<Key, PagedResponse<AlbumResponse>> cache = new HashMap<>();

    public PagedResponse<AlbumResponse> get(Key key) {
        return cache.get(key);
    }

    public void put(Key key, PagedResponse<AlbumResponse> value) {
        cache.put(key, value);
    }

    public void invalidateAll() {
        cache.clear();
    }

    public static Key keyOf(
        String genreName,
        String trackTitle,
        boolean nativeQuery,
        Pageable pageable) {
        return new Key(
            normalize(genreName),
            normalize(trackTitle),
            nativeQuery,
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sortToString(pageable.getSort()));
    }

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String sortToString(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Sort.Order order : sort) {
            if (!sb.isEmpty()) {
                sb.append(';');
            }
            sb.append(order.getProperty()).append(',').append(order.getDirection().name());
        }
        return sb.toString();
    }

    public static final class Key {
        private final String genreName;
        private final String trackTitle;
        private final boolean nativeQuery;
        private final int page;
        private final int size;
        private final String sort;

        public Key(String genreName, String trackTitle, boolean nativeQuery, int page, int size, String sort) {
            this.genreName = genreName;
            this.trackTitle = trackTitle;
            this.nativeQuery = nativeQuery;
            this.page = page;
            this.size = size;
            this.sort = sort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return nativeQuery == key.nativeQuery
                    && page == key.page
                    && size == key.size
                    && Objects.equals(genreName, key.genreName)
                    && Objects.equals(trackTitle, key.trackTitle)
                    && Objects.equals(sort, key.sort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(genreName, trackTitle, nativeQuery, page, size, sort);
        }
    }
}

