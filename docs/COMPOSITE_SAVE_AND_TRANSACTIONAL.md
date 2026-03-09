# Сохранение связанных сущностей и демонстрация @Transactional

## Метод сохранения нескольких связанных сущностей

Сервис `CompositeSaveService` сохраняет цепочку **Artist → Album → Tracks** в одном вызове:

1. Сохраняется артист.
2. Сохраняется альбом (с ссылкой на артиста).
3. Сохраняются треки (с ссылкой на альбом).

## Частичное сохранение без @Transactional

Метод `saveArtistWithAlbumAndTracksWithoutTransaction(...)` **не** помечен `@Transactional`. Каждый `repository.save()` выполняется в своей транзакции (по умолчанию у каждого метода репозитория) и сразу коммитится.

При передаче `simulateFailureAfterAlbum = true` после сохранения альбома выбрасывается исключение. К этому моменту уже закоммичены:

- артист;
- альбом.

Треки не сохраняются. В БД остаётся **частично** сохранённая цепочка — это демонстрирует риск при отсутствии общей транзакции.

**Пример запроса:**

```http
POST /api/demo/composite-save/without-transaction?simulateFailure=true
Content-Type: application/json

{
  "artistName": "Test Artist",
  "albumTitle": "Test Album",
  "releaseYear": 2024,
  "tracks": [
    { "title": "Track 1", "durationSeconds": 200 },
    { "title": "Track 2", "durationSeconds": 180 }
  ]
}
```

Ответ: 500 с сообщением об ошибке. В БД при этом останутся артист и альбом (без треков).

## Полный откат с @Transactional

Метод `saveArtistWithAlbumAndTracksWithTransaction(...)` помечен `@Transactional(rollbackFor = Exception.class)`. Вся операция выполняется в одной транзакции.

При передаче `simulateFailureAfterAlbum = true` после сохранения альбома выбрасывается исключение. Spring откатывает транзакцию, и в БД **не остаётся** ни артиста, ни альбома, ни треков — операция откатывается целиком.

**Пример запроса:**

```http
POST /api/demo/composite-save/with-transaction?simulateFailure=true
Content-Type: application/json

{
  "artistName": "Rollback Artist",
  "albumTitle": "Rollback Album",
  "releaseYear": 2024,
  "tracks": [
    { "title": "Track 1", "durationSeconds": 200 }
  ]
}
```

Ответ: 500. В БД после этого запроса не будет ни «Rollback Artist», ни «Rollback Album».

## Вывод

- **Без общей транзакции** при ошибке в середине сценария в БД может остаться часть данных (частичное сохранение).
- **С @Transactional** при любой ошибке откатывается вся операция, целостность данных сохраняется.

Для операций, которые должны выполняться «всё или ничего», связанные сохранения нужно выполнять в одном методе с `@Transactional`.
