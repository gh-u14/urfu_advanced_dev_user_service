# User Service

User Service. Java 21, Spring Boot 3.5, PostgreSQL

## Запуск (Docker)

```bash
docker compose up --build -d
```

| Контейнер | Порт | Назначение |
|-----------|------|------------|
| `users-postgres` | `5432` | PostgreSQL (`users_db`) |
| `users-service` | `9000` | User Service |

Сервис: `http://localhost:9000`  
БД: `localhost:5432`, user/password `postgres`/`postgres`

При старте приложения выполняется `schema.sql` (таблицы `users`, `roles`, `user_roles` + seed ролей)

Локальный запуск без Docker-образа сервиса (Postgres все равно через compose):

```bash
docker compose up -d postgres
cd user-service && mvn spring-boot:run
```

## API (`/api/v1`)

| Method | Path | Auth | Описание |
|--------|------|------|----------|
| `POST` | `/auth/register` | нет | регистрация + роль `subscriber` |
| `POST` | `/auth/login` | нет | вход, ответ `{ "token": "..." }` |
| `GET` | `/users/me` | Bearer JWT | свой профиль |
| `PATCH` | `/users/me` | Bearer JWT | обновить профиль |
| `GET` | `/users/{id}` | нет* | публичный профиль |
| `GET` | `/users?ids=1,2` | нет* | batch профилей |
| `PUT` | `/users/{id}/roles/{role}` | JWT + `administrator` | выдать роль |
| `DELETE` | `/users/{id}/roles/{role}` | JWT + `administrator` | забрать роль |

Открыты для вызовов Messages Service.

### Регистрация

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstname": "Мария",
  "lastname": "Орлова",
  "email": "maria.orlova@example.com",
  "phone_number": "+79876543210",
  "password": "secret123"
}
```

### Вход

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "maria.orlova@example.com",
  "password": "secret123"
}
```

JWT содержит `sub` (user id) и claim `roles`

### Ошибки

```json
{ "error": "email_taken", "message": "Email уже занят" }
```

## Структура

```
lr_7/
├── docker-compose.yml
├── README.md
└── user-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/userservice/
        ├── config/       # Cors, JwtAuthFilter
        ├── controller/   # Auth, User, Role
        ├── service/
        ├── dao/
        ├── dto/
        ├── model/
        ├── util/
        └── exception/
```
