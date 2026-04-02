# FinanceApplication

This repository contains a Spring Boot backend for finance record management with JWT authentication, role-based authorization, dashboard analytics, and audit logging.

## Documentation Index

Use these separate README files for final submission:

- [Setup Instructions](docs/README-SETUP.md)
- [API Usage and Contract](docs/README-API.md)
- [Role and Permission Model](docs/README-ROLES.md)
- [Implementation Notes](docs/README-IMPLEMENTATION.md)

## Quick Run

1. Create PostgreSQL database `finance`.
2. Set required env vars (`ADMIN_EMAIL`, `JWT_SECRET`).
3. Start app:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Default base URL: `http://localhost:8080/api/v1`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Testing

Run all tests:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Current automated tests include unit tests for:

- `UserServiceImpl`
- `CategoryServiceImpl`
- `FinancialRecordServiceImpl`
