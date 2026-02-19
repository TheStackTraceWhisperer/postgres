# application

Basic Spring Data JPA project using PostgreSQL.

## Prerequisites
- Java 17+
- Maven
- PostgreSQL running via the root `docker-compose.yml`

## Run PostgreSQL (from repo root)
```bash
docker compose up -d
```

## Run the app
```bash
cd /home/samuel/projects/postgres/application
mvn spring-boot:run
```

## Run tests
```bash
cd /home/samuel/projects/postgres/application
mvn test
```

## Notes
- Datasource defaults to `jdbc:postgresql://localhost:5432/app_db` with `app_user/app_password`.
- The app logs the widget count on startup (from the `widgets` table created by the init SQL).

