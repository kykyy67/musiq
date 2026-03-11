# МУЗЫКАЛЬНЫЙ КАТАЛОГ

### REST API проект на Java, фреймворк Spring, Maven.
#### Сервис должен принимать и обрабатыватьь данные об исполнителях, альбомах и треках. 

1. Spring Boot приложение.
2. Реализовано REST API для одной ключевой сущности своей предметной области (domain).
3. Реализовано:
- GET endpoint с @RequestParam
- GET endpoint с @PathVariable
4. Реализованы слои: Controller → Service → Repository.
5. Реализовано DTO и mapper между Entity и API-ответом.
6. Настроин Checkstyle и привести код к стилю.
7. Подключена реляционная БД к проекту.
9. В модели данных реализовано 5 сущностей:
- минимум одну связь OneToMany
- минимум одну связь ManyToMany
10. Реализованы CRUD операции.
11. Настроины использование CascadeType и FetchType.
12. Продемонстрирована проблему N+1 и решить её через @EntityGraph или fetch join.
13. Реализован метод, сохраняющий несколько связанных сущностей. Продемонстрировано частичное сохранение данных без @Transactional и полное откатывание операции с @Transactional при возникновении ошибки.
14. Нарисована ER-диаграмма с указанием PK/FK и связей.

#### Аналоги
- VK Musiq
- Яндекс Музыка

<img width="993" height="673" alt="image" src="https://github.com/user-attachments/assets/b6cac6c7-4290-4d2b-b7b1-695c6fe6197e" />


[Сонар](https://sonarcloud.io/project/overview?id=kykyy67_musiq)
