# Minecraft Mods Catalog

REST API-каталог модов для Minecraft на Spring Boot.

## О проекте

Проект предоставляет простой HTTP API для:
- получения списка модов;
- фильтрации модов по автору;
- получения мода по `id`;
- добавления нового мода.

Архитектура: `controller -> service -> repository`, используются DTO и mapper.

## Технологии

- Java 25
- Spring Boot 4
- Maven
- Lombok
- PostgreSQL (конфигурация есть в `application.properties`)

## Важный нюанс по хранилищу

Сейчас в коде подключен `InMemoryModRepository`, поэтому данные хранятся в памяти приложения и теряются после перезапуска.
Параметры PostgreSQL в `application.properties` пока не используются текущей реализацией репозитория.

## Требования

- JDK 25
- Maven 3.9+ (или Maven Wrapper из проекта)

## Запуск

### Через Maven Wrapper (рекомендуется)

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

### Сборка и запуск jar

```bash
./mvnw clean package
java -jar target/minecraft-mods-catalog-0.0.1-SNAPSHOT.jar
```

По умолчанию приложение стартует на `http://localhost:8080`.

## API

Базовый путь: `/api/mods`

### 1) Получить все моды

```http
GET /api/mods
```

### 2) Получить моды по автору

```http
GET /api/mods?author=mezz
```

### 3) Получить мод по id

```http
GET /api/mods/{id}
```

### 4) Создать мод

```http
POST /api/mods
Content-Type: application/json
```

Пример тела запроса:

```json
{
  "name": "Sodium",
  "description": "Оптимизация производительности",
  "author": "jellysquid3",
  "version": "0.5.0",
  "downloadCount": 120000
}
```

## Стартовые данные

При запуске приложение инициализирует несколько модов (`OptiFine`, `JEI`, `Create`) через `CommandLineRunner`.

## Тесты

```bash
./mvnw test
```
