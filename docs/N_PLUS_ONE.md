# Проблема N+1 и её решение

## В чём проблема

При загрузке списка альбомов и маппинге в DTO для каждого альбома вызываются `album.getArtist()` и `album.getGenres()`. При `FetchType.LAZY` это приводит к дополнительным запросам в БД:

- 1 запрос: `SELECT * FROM albums [WHERE title LIKE ...]`
- N запросов: для каждого альбома `SELECT * FROM artists WHERE id = ?`
- N запросов: для каждого альбома `SELECT * FROM album_genres JOIN genres ... WHERE album_id = ?`

Итого для 10 альбомов — до **1 + 10 + 10 = 21** запросов (N+1).

## Решение: fetch join в репозитории

В `AlbumRepository` добавлены методы с явным fetch join:

```java
@Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.artist LEFT JOIN FETCH a.genres")
List<Album> findAllWithArtistAndGenres();

@Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.artist LEFT JOIN FETCH a.genres "
    + "WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))")
List<Album> findByTitleWithArtistAndGenres(@Param("title") String title);
```

Один запрос загружает альбомы вместе с `artist` и `genres`, при маппинге в DTO дополнительных запросов нет.

В API списка альбомов (`GET /api/albums`) используется `getAlbumsByTitleWithArtistAndGenres(title)`, поэтому N+1 при отдаче списка альбомов устранён.

## Альтернатива: @EntityGraph

Того же эффекта можно добиться через `@EntityGraph`:

```java
@EntityGraph(attributePaths = {"artist", "genres"})
@Query("SELECT a FROM Album a")
List<Album> findAllWithArtistAndGenres();
```

В данном проекте использован fetch join для явного контроля над запросом и совместимости с фильтром по `title`.
