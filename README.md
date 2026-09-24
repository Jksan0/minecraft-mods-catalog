# Minecraft Mods Catalog

Веб-приложение на Spring Boot для каталога Minecraft-модов с авторским, категорийным и теговым описанием, версиями модов, фильтрацией, связями между сущностями, а также демонстрацией задач на параллельность и N+1.

## Что это

Проект предназначен для работы с каталогом модов и демонстрации:
- CRUD для модов, авторов, категорий и тегов;
- связи между сущностями (`Mod`, `Author`, `Category`, `Tag`, `ModVersion`);
- фильтрации и поиска по автору, категории и тегам;
- пагинации на уровне API/фронта;
- учебных примеров по N+1 и конкурентному доступу;
- запуска через Docker Compose.

## Технологии

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Docker / Docker Compose
- Swagger / OpenAPI
- Maven

## Структура проекта

```text
minecraft-mods-catalog/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/example/minecraftmodscatalog/
│  │  │     ├─ controller/
│  │  │     ├─ service/
│  │  │     ├─ repository/
│  │  │     ├─ entity/
│  │  │     ├─ dto/
│  │  │     └─ mapper/
│  │  └─ resources/
│  │     ├─ application.properties
│  │     └─ static/
│  └─ test/
├─ Dockerfile
├─ docker-compose.yml
├─ .env
├─ pom.xml
├─ mvnw
├─ README.md
└─ logs/
```

## Основные сущности

- Author — автор мода
- Category — категория мода
- Tag — тег/метка мода
- Mod — сам мод
- ModVersion — версия мода с количеством скачиваний

## Быстрый старт

### 1. Установите зависимости

Для запуска локально:
- Java 21+
- Maven 3.9+
- Docker Desktop (если запускаете через контейнеры)

### 2. Создайте .env

Файл `.env` должен лежать в корне проекта:

```env
APP_PORT=8081
POSTGRES_PORT=15432
POSTGRES_DB=minecraft_mods
POSTGRES_USER=postgres
POSTGRES_PASSWORD=12345
```

### 3. Запуск через Docker Compose

```bash
docker compose up --build
```

После запуска:
- приложение: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- PostgreSQL: localhost:15432

### 4. Запуск локально без Docker

```bash
./mvnw clean spring-boot:run
```

## Docker

Проект содержит два сервиса:
- `app` — Spring Boot приложение
- `postgres` — база PostgreSQL

Файл сборки:
- `Dockerfile`
- `docker-compose.yml`

## Полезные команды

Запуск контейнеров:
```bash
docker compose up --build
```

Остановка:
```bash
docker compose down
```

Полный сброс данных и пересборка:
```bash
docker compose down -v --remove-orphans
docker compose up --build
```

Просмотр логов:
```bash
docker compose logs -f app
docker compose logs -f postgres
```

## API

Основные endpoints:

### Авторы
- `GET /api/authors`
- `GET /api/authors/{id}`
- `POST /api/authors`
- `PUT /api/authors/{id}`
- `DELETE /api/authors/{id}`

### Категории
- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### Теги
- `GET /api/tags`
- `GET /api/tags/{id}`
- `POST /api/tags`
- `PUT /api/tags/{id}`
- `DELETE /api/tags/{id}`

### Моды
- `GET /api/mods`
- `GET /api/mods/{id}`
- `POST /api/mods`
- `PUT /api/mods/{id}`
- `DELETE /api/mods/{id}`

### Версии модов
- `GET /api/mod-versions`
- `GET /api/mod-versions/{id}`
- `POST /api/mod-versions`
- `PUT /api/mod-versions/{id}`
- `DELETE /api/mod-versions/{id}`

### Демонстрационные endpoint'ы
- `POST /api/demo/async`
- `GET /api/demo/async/{taskId}`
- `GET /api/demo/counter?threads=50&incrementsPerThread=1000`

## Пример создания мода

Пример JSON для `POST /api/mods`:

```json
[
  {
    "name": "Create",
    "description": "Автоматизация и механика для крупных производственных цепочек.",
    "authorName": "Riva",
    "categoryNames": ["Автоматизация"],
    "tagNames": ["automation", "machinery", "tech"],
    "versions": [
      {
        "versionName": "1.0.0",
        "downloadCount": 125000
      }
    ]
  }
]
```

Важно:
- `versionName` — это версия самого мода, например `1.0.0`, `2.3.1`, а не версия Minecraft;
- `categoryNames` и `tagNames` могут содержать несколько значений.

## Проверка работоспособности

После запуска:

```bash
docker compose ps
```

или открыть:
- Swagger UI
- страницу главного приложения

## Примечания

- Данные хранятся в Docker volume `pg_data`.
- Для локального запуска может использоваться PostgreSQL из контейнера.
- В проекте есть frontend-часть в `src/main/resources/static` и backend API в Java.

## Лицензия

На данный момент проект предназначен для локального использования и демонстрации. При необходимости можно добавить лицензию отдельно.
