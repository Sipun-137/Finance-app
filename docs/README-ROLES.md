# Role and Permission Model

## Roles

The application uses these authority values:

- `ROLE_ADMIN`
- `ROLE_ANALYST`
- `ROLE_VIEWER`

## Role Behavior

- One user can have multiple roles.
- New users default to `ROLE_VIEWER` if role is not provided.
- You cannot remove the only role from a user (`removeRole` guard in service).
- User status can be `ACTIVE` or `INACTIVE`.
- Inactive users cannot login (`DisabledException` branch in auth controller).

## Endpoint-Level Access Matrix

| Endpoint | ADMIN | ANALYST | VIEWER |
| --- | --- | --- | --- |
| `POST /api/v1/auth/login` | Yes | Yes | Yes |
| `POST /api/v1/users` | Yes | No | No |
| `GET /api/v1/users` | Yes | No | No |
| `GET /api/v1/users/{id}` | Yes | Yes | Yes |
| `PATCH /api/v1/users/{id}` | Yes | No | No |
| `PATCH /api/v1/users/{id}/activate` | Yes | No | No |
| `PATCH /api/v1/users/{id}/deactivate` | Yes | No | No |
| `POST /api/v1/users/{id}/roles` | Yes | No | No |
| `DELETE /api/v1/users/{id}/roles` | Yes | No | No |
| `POST /api/v1/categories` | Yes | Yes | No |
| `GET /api/v1/categories` | Yes | Yes | Yes |
| `GET /api/v1/categories/type/{type}` | Yes | Yes | Yes |
| `GET /api/v1/categories/{id}` | Yes | Yes | Yes |
| `PUT /api/v1/categories/{id}` | Yes | Yes | No |
| `DELETE /api/v1/categories/{id}` | Yes | No | No |
| `POST /api/v1/record` | Yes | Yes | No |
| `GET /api/v1/record` | Yes | Yes | Yes |
| `GET /api/v1/record/{id}` | Yes | Yes | Yes |
| `PUT /api/v1/record/{id}` | Yes | Yes | No |
| `DELETE /api/v1/record/{id}` | Yes | Yes | No |
| `GET /api/v1/record/recent` | Yes | Yes | Yes |
| `GET /api/v1/dashboard/overview` | Yes | Yes | Yes |
| `GET /api/v1/dashboard/summary` | Yes | Yes | Yes |
| `GET /api/v1/dashboard/category-breakdown` | Yes | Yes | No |
| `GET /api/v1/dashboard/monthly-trends` | Yes | Yes | No |
| `GET /api/v1/dashboard/recent-activity` | Yes | Yes | Yes |
| `GET /api/v1/audit/filter` | Yes | No | No |

## Data Scope Rules (Current Implementation)

- Record and dashboard queries are scoped by authenticated user ID.
- Categories are global and shared.
- Audit log filters are admin-only.
- User `GET /users/{id}` currently allows any authenticated role and is not scoped to self.

## Security Enforcement Layers

- Route security and JWT filter in `SecurityConfig` and `JwtFilter`.
- Method-level authorization with `@PreAuthorize`.
- Password hashing with BCrypt.
- Access denied and validation/global exception handlers return structured JSON responses.

## Authentication Flow

1. User logs in via `/api/v1/auth/login`.
2. Server validates credentials and returns JWT.
3. Client sends `Authorization: Bearer <token>`.
4. `JwtFilter` validates token and sets authentication.
5. Method-level role checks decide endpoint access.
