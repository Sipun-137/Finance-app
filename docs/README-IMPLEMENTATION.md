# Implementation Notes

## Tech Stack and Why Chosen

| Technology | Why it was chosen |
| --- | --- |
| Java 25 | Modern Java features and strong ecosystem support. |
| Spring Boot 4.0.5 | Fast backend development with production-grade defaults. |
| Spring Web MVC | Clean REST controller model. |
| Spring Data JPA + Hibernate | Rapid persistence layer with query abstractions and entity mapping. |
| PostgreSQL | Reliable relational storage with good aggregation support for analytics. |
| Spring Security | Standardized authentication and authorization support. |
| JWT (jjwt 0.12.6) | Stateless auth suitable for API-first architecture. |
| Jakarta Validation | Request-level validation close to DTO contract. |
| Lombok | Reduces boilerplate for DTOs/entities/services. |
| Springdoc OpenAPI | Swagger UI and OpenAPI generation for API exploration. |

## Local Setup and Run Commands

Build:

```bash
./mvnw clean install
```

Run:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

Detailed setup is in [README-SETUP.md](README-SETUP.md).

## Environment Variables

Required:

- `ADMIN_EMAIL`
- `JWT_SECRET` (must be Base64 encoded, because JWT service decodes with `Decoders.BASE64.decode`)

Profile-based DB vars:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` for `staging`/`prod`

## Architecture Summary

- `controller`: HTTP/API layer
- `service`: business logic and orchestration
- `repo`: data access and query methods
- `model`: JPA entities
- `DTO`: request/response contracts
- `config/security`: auth, JWT filter, CORS, access denied handling
- `audit`: annotation + aspect + persistence for operation logs
- `exception`: global handlers and custom exceptions

## Database Schema and Models

Main entities:

- `users`
  - `id`, `name`, `email`, `password_hash`, `status`, `roles`, `created_at`, `updated_at`
- `categories`
  - `id`, `name`, `type` (`INCOME`/`EXPENSE`), `color`, `created_at`, `updated_at`
- `financial_records`
  - `id`, `user_id`, `category_id`, `amount`, `type`, `description`, `record_date`, `is_deleted`, `created_at`, `updated_at`
- `audit_logs`
  - `id`, `user_id`, `action`, `resource`, `resource_id`, `meta`, `created_at`

Relationships:

- `financial_records.user_id -> users.id` (many-to-one)
- `financial_records.category_id -> categories.id` (many-to-one)
- `audit_logs.user_id -> users.id` (many-to-one)

Soft delete:

- `financial_records` uses `is_deleted` and `@SQLRestriction("is_deleted = false")`.

## API Contract With Sample Requests/Responses

Complete endpoint contract and examples are in [README-API.md](README-API.md).

Highlights:

- Auth: `/api/v1/auth/login`
- Users: `/api/v1/users`
- Categories: `/api/v1/categories`
- Records: `/api/v1/record`
- Dashboard: `/api/v1/dashboard/*`
- Audit: `/api/v1/audit/filter`

Standard response envelope:

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

## Test Strategy and How to Run Tests

Current state:

- Unit tests are implemented for core service-layer business logic.
- Smoke test validates test-module wiring (`FinanceApplicationTests`).

Implemented unit test classes:

- `CategoryServiceImplTest` (`7` tests)
- `UserServiceImplTest` (`9` tests)
- `FinancialRecordServiceImplTest` (`11` tests)

Current suite total: `28` tests.

Run all tests:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

Run a single test class:

```bash
./mvnw -Dtest=FinancialRecordServiceImplTest test
```

```bash
./mvnw -Dtest=CategoryServiceImplTest test
```

```bash
./mvnw -Dtest=UserServiceImplTest test
```

Recommended testing strategy for improvement:

- Repository tests for custom queries/specifications.
- Controller tests for authorization and validation behavior.
- Integration tests for full auth -> protected endpoint flow.

## Known Limitations

- Current automated tests focus mainly on service layer; repository and controller layers need more coverage.
- No refresh token flow; JWT is short-lived access-token only.
- No database migration tool (Flyway/Liquibase) yet.
- `dev` profile currently contains local DB credentials in config file.
- Security allows `/api/v1/health`, but no dedicated health controller is currently implemented.
- Role persistence is represented as `Set<Role>` in `User`; schema behavior should be validated across environments.

## Next Improvements

1. Add repository and controller test suites.
2. Introduce Flyway/Liquibase migrations for deterministic schema evolution.
3. Add refresh token + token revocation strategy.
4. Add rate limiting and login-attempt protections.
5. Add explicit health/readiness endpoints.
6. Harden role persistence mapping with explicit collection-table strategy.
7. Add CI pipeline (build, test, static checks, packaging).
