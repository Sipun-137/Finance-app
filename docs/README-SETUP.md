# Setup Instructions

## 1. Prerequisites

- Java 25 (required by `pom.xml`)
- Maven Wrapper (already in repo: `mvnw`, `mvnw.cmd`)
- PostgreSQL 14+

## 2. Configuration Profiles

Current default profile is in `src/main/resources/application.yml`:

- `spring.profiles.active=dev`

Behavior by profile:

- `dev`: DB settings come from `application-dev.yml` (hardcoded local values).
- `staging` and `prod`: DB settings come from env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

## 3. Required Environment Variables

These values are required by `application.yml`:

| Variable | Required | Description | Example |
| --- | --- | --- | --- |
| `ADMIN_EMAIL` | Yes | Email used by startup admin initializer | `admin@finance.com` |
| `JWT_SECRET` | Yes | Base64-encoded JWT signing secret | `bXlfMzJfYnl0ZV9iYXNlNjRfc2VjcmV0X3BsYWNlaG9sZGVy` |

Only needed for `staging` or `prod` profile:

| Variable | Required in Staging/Prod | Description | Example |
| --- | --- | --- | --- |
| `DB_URL` | Yes | JDBC URL | `jdbc:postgresql://localhost:5432/finance` |
| `DB_USERNAME` | Yes | DB username | `postgres` |
| `DB_PASSWORD` | Yes | DB password | `postgres` |

Optional:

| Variable | Description | Example |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Override active profile | `staging` |

## 4. Create Database

For default `dev` profile, create DB named `finance`:

```sql
CREATE DATABASE finance;
```

## 5. Set Environment Variables

PowerShell (current terminal session):

```powershell
$env:ADMIN_EMAIL="admin@finance.com"
$env:JWT_SECRET="bXlfMzJfYnl0ZV9iYXNlNjRfc2VjcmV0X3BsYWNlaG9sZGVy"
```

If using `staging`/`prod`:

```powershell
$env:SPRING_PROFILES_ACTIVE="staging"
$env:DB_URL="jdbc:postgresql://localhost:5432/finance"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
```

Linux/macOS:

```bash
export ADMIN_EMAIL="admin@finance.com"
export JWT_SECRET="bXlfMzJfYnl0ZV9iYXNlNjRfc2VjcmV0X3BsYWNlaG9sZGVy"
```

Generate a secure Base64 JWT secret (PowerShell):

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

## 6. Build and Run

Build:

```bash
./mvnw clean install
```

Windows:

```powershell
.\mvnw.cmd clean install
```

Run tests (recommended before starting the app):

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

Run:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

App URL: `http://localhost:8080`

## 7. First Login

On first startup, `AdminInitializer` creates a default admin user if `ADMIN_EMAIL` does not exist:

- Email: value of `ADMIN_EMAIL`
- Password: `Admin#1234`
- Role: `ROLE_ADMIN`

Login endpoint:

```http
POST /api/v1/auth/login
```

Swagger/OpenAPI UI:

- `http://localhost:8080/swagger-ui/index.html`

## 8. Notes

- `.env` exists in the repository, but Spring Boot does not automatically load `.env` by default.
- If you use `.env`, load values through your IDE run configuration or shell tooling.
- In `dev`, schema is managed by Hibernate with `spring.jpa.hibernate.ddl-auto=update`.

