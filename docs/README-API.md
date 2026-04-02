# API Usage and Documentation

## Base URL

- `http://localhost:8080/api/v1`

## Authentication

Use JWT bearer token for all protected routes:

```http
Authorization: Bearer <token>
```

Public route:

- `POST /auth/login`

## Response Contract

Standard response wrapper (`ApiResponse<T>`):

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

Paged endpoints use:

```json
{
  "success": true,
  "message": "Records fetched successfully",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "last": true
  }
}
```

## Endpoint Summary

| Module | Method | Path | Access |
| --- | --- | --- | --- |
| Auth | POST | `/auth/login` | Public |
| Users | POST | `/users` | `ROLE_ADMIN` |
| Users | GET | `/users` | `ROLE_ADMIN` |
| Users | GET | `/users/{id}` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Users | PATCH | `/users/{id}` | `ROLE_ADMIN` |
| Users | PATCH | `/users/{id}/deactivate` | `ROLE_ADMIN` |
| Users | PATCH | `/users/{id}/activate` | `ROLE_ADMIN` |
| Users | POST | `/users/{id}/roles` | `ROLE_ADMIN` |
| Users | DELETE | `/users/{id}/roles?role=ROLE_ANALYST` | `ROLE_ADMIN` |
| Categories | POST | `/categories` | `ROLE_ADMIN`, `ROLE_ANALYST` |
| Categories | GET | `/categories` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Categories | GET | `/categories/type/{type}` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Categories | GET | `/categories/{id}` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Categories | PUT | `/categories/{id}` | `ROLE_ADMIN`, `ROLE_ANALYST` |
| Categories | DELETE | `/categories/{id}` | `ROLE_ADMIN` |
| Record | POST | `/record` | `ROLE_ADMIN`, `ROLE_ANALYST` |
| Record | GET | `/record` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Record | GET | `/record/{id}` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Record | PUT | `/record/{id}` | `ROLE_ADMIN`, `ROLE_ANALYST` |
| Record | DELETE | `/record/{id}` | `ROLE_ADMIN`, `ROLE_ANALYST` |
| Record | GET | `/record/recent?limit=5` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Dashboard | GET | `/dashboard/overview` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Dashboard | GET | `/dashboard/summary` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Dashboard | GET | `/dashboard/category-breakdown` | `ROLE_ADMIN`, `ROLE_ANALYST` |
| Dashboard | GET | `/dashboard/monthly-trends` | `ROLE_ADMIN`, `ROLE_ANALYST` |
| Dashboard | GET | `/dashboard/recent-activity?limit=10` | `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_VIEWER` |
| Audit | GET | `/audit/filter` | `ROLE_ADMIN` |

## Sample Requests and Responses

### 1. Login

Request:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "userName": "admin@finance.com",
  "password": "Admin#1234"
}
```

Response:

```json
{
  "success": true,
  "message": "Login Successful",
  "data": {
    "token": "<jwt>",
    "id": "88e2...",
    "name": "Admin",
    "email": "admin@finance.com",
    "roles": ["ROLE_ADMIN"]
  }
}
```

### 2. Create User (Admin)

Request:

```http
POST /api/v1/users
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "name": "Anita Sharma",
  "email": "anita@example.com",
  "password": "StrongPass#123",
  "role": "ROLE_ANALYST"
}
```

Response:

```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "f2d1...",
    "name": "Anita Sharma",
    "email": "anita@example.com",
    "status": "ACTIVE",
    "roles": ["ROLE_ANALYST"],
    "createdAt": "2026-04-02T12:40:31",
    "updatedAt": "2026-04-02T12:40:31"
  }
}
```

### 3. Create Category

Request:

```http
POST /api/v1/categories
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "name": "Salary",
  "type": "INCOME",
  "color": "#22AA66"
}
```

### 4. Create Record

Request:

```http
POST /api/v1/record
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "amount": 125000.00,
  "type": "INCOME",
  "categoryId": "a6c9...",
  "description": "March salary",
  "recordDate": "2026-03-31"
}
```

Response:

```json
{
  "success": true,
  "message": "Record created successfully",
  "data": {
    "id": "b7a1...",
    "amount": 125000.00,
    "type": "INCOME",
    "categoryId": "a6c9...",
    "categoryName": "Salary",
    "categoryColor": "#22AA66",
    "description": "March salary",
    "recordDate": "2026-03-31",
    "createdAt": "2026-04-02T12:44:10",
    "updatedAt": "2026-04-02T12:44:10"
  }
}
```

### 5. Filter Records

Request:

```http
GET /api/v1/record?type=EXPENSE&from=2026-03-01&to=2026-03-31&searchTerm=food&page=0&size=10
Authorization: Bearer <jwt>
```

Supported query filters:

- `searchTerm`
- `type` (`INCOME` or `EXPENSE`)
- `categoryId`
- `from`
- `to`
- `sortBy` (default `recordDate`)
- `sortDir` (default `desc`)
- plus pageable: `page`, `size`, `sort`

### 6. Dashboard Summary

Request:

```http
GET /api/v1/dashboard/summary?from=2026-03-01&to=2026-03-31
Authorization: Bearer <jwt>
```

Response shape:

```json
{
  "success": true,
  "message": "Summary fetched",
  "data": {
    "totalIncome": 125000.00,
    "totalExpense": 32000.00,
    "netBalance": 93000.00,
    "totalRecords": 23,
    "periodLabel": "Mar 2026 - Mar 2026"
  }
}
```

### 7. Audit Filter (Admin)

Request:

```http
GET /api/v1/audit/filter?action=CREATE_RECORD&resource=record&page=0&size=20
Authorization: Bearer <jwt>
```

Filter params:

- `userId`
- `action`
- `resource`
- `fromDate` (ISO datetime)
- `toDate` (ISO datetime)

## Error Responses

Validation and domain errors are returned by global handlers:

```json
{
  "success": false,
  "message": "Email already in use: anita@example.com",
  "data": null
}
```

JWT failures from filter return HTTP 401 with shape:

```json
{
  "success": false,
  "message": "Invalid JWT token.",
  "status": 401
}
```

## Interactive API Docs

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
