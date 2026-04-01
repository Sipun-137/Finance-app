# Finance Data Processing and Access Control Backend

A robust Spring Boot backend application for managing financial records with role-based access control, designed to demonstrate backend architecture, data modeling, business logic, and security implementation.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Role-Based Access Control](#role-based-access-control)
- [Security](#security)
- [Error Handling](#error-handling)
- [Assumptions & Design Decisions](#assumptions--design-decisions)
- [Future Enhancements](#future-enhancements)

## 🎯 Overview

This application serves as a backend for a finance dashboard system where users with different roles interact with financial records based on their permissions. The system supports comprehensive user management, financial record operations, and provides analytics through dashboard APIs.

**Key Objectives:**

- Demonstrate clean backend architecture with proper separation of concerns
- Implement robust role-based access control (RBAC)
- Handle financial data with precision and validation
- Provide RESTful APIs for frontend integration
- Ensure data integrity and security

## ✨ Features

### 1. User & Role Management

- **User CRUD Operations**: Create, read, update users
- **Role Assignment**: Assign and remove roles dynamically
- **User Status Management**: Activate/deactivate user accounts
- **Three-Tier Role System**:
  - `VIEWER`: Read-only access to dashboard data
  - `ANALYST`: Read access to records and analytics
  - `ADMIN`: Full access to manage users and records

### 2. Financial Records Management

- Create, read, update, and delete financial records
- Support for Income and Expense transactions
- Category-based organization
- Soft delete functionality (records are never physically removed)
- BigDecimal precision for monetary values
- Transaction date tracking separate from audit timestamps

### 3. Dashboard & Analytics APIs

_(Planned/In Development)_

- Total income calculation
- Total expenses calculation
- Net balance computation
- Category-wise summaries
- Time-based trends (monthly/weekly)
- Recent activity tracking

### 4. Security & Authentication

- **JWT-based Authentication**: Secure token-based auth
- **Password Encryption**: BCrypt hashing
- **Role-based Authorization**: Method-level security with `@PreAuthorize`
- **Admin Auto-initialization**: Default admin account creation on startup
- **Custom Access Denied Handling**: User-friendly error responses

### 5. Auditing & Logging

- **Aspect-Oriented Auditing**: Automatic logging of critical operations
- **Audit Trail**: Track who did what and when
- **Custom `@Auditable` Annotation**: Declarative audit logging

### 6. Input Validation & Error Handling

- Request DTO validation using Jakarta Validation
- Global exception handling with `@RestControllerAdvice`
- Standardized API response format
- Appropriate HTTP status codes
- Detailed error messages for debugging

## 🛠 Technology Stack

### Core Framework

- **Java 25**: Latest Java language features
- **Spring Boot 4.0.5**: Backend framework
- **Spring Data JPA**: Database abstraction and ORM
- **Spring Security**: Authentication and authorization
- **Spring Web MVC**: RESTful API development

### Database

- **PostgreSQL**: Primary relational database
- **Hibernate**: ORM implementation

### Security

- **JWT (JSON Web Tokens)**: Stateless authentication
- **jjwt 0.12.6**: JWT library
- **BCrypt**: Password hashing

### Additional Libraries

- **Lombok**: Boilerplate code reduction
- **Jakarta Validation**: Input validation
- **SLF4J**: Logging facade

### Build Tool

- **Maven**: Dependency management and build automation

## 🏗 Architecture

### Project Structure

```
com.finance.financeapplication
├── auth/                          # Authentication & JWT
│   ├── controller/                # Login endpoints
│   ├── service/                   # JWT & UserDetails services
│   ├── model/                     # UserPrincipal
│   └── DTO/                       # Login request/response
├── user/                          # User management
│   ├── controller/                # User CRUD endpoints
│   ├── service/                   # Business logic
│   ├── repository/                # Data access
│   ├── model/                     # User entity
│   └── DTO/                       # Request/response objects
├── record/                        # Financial records
│   ├── model/                     # FinancialRecord & Category entities
│   └── (controllers/services planned)
├── audit/                         # Audit logging
│   ├── aspect/                    # AOP audit aspect
│   ├── service/                   # Audit log service
│   ├── repository/                # Audit log repository
│   ├── model/                     # AuditLog entity
│   └── annotation/                # @Auditable
├── config/                        # Configuration
│   └── security/                  # Security config, filters, handlers
├── common/                        # Shared utilities
│   ├── DTO/                       # ApiResponse, PagedResponse
│   └── enums/                     # Role, UserStatus, RecordType
└── exception/                     # Exception handling
    ├── GlobalExceptionHandler     # Central error handling
    └── common/                    # Custom exceptions
```

### Layered Architecture

1. **Controller Layer**: Handles HTTP requests, validation, and response formatting
2. **Service Layer**: Contains business logic and orchestration
3. **Repository Layer**: Data access and persistence
4. **Security Layer**: Authentication, authorization, and filters
5. **Cross-cutting Concerns**: Logging, auditing, exception handling

## 🚀 Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.8+
- PostgreSQL 14+
- IDE (IntelliJ IDEA recommended)

### Environment Setup

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd FinanceApplication
   ```

2. **Configure Database**

   Create a PostgreSQL database:

   ```sql
   CREATE DATABASE finance_db;
   ```

3. **Set Environment Variables**

   Create a `.env` file in the project root:

   ```env
   ADMIN_EMAIL=admin@finance.com
   JWT_SECRET=your-secret-key-min-256-bits
   ```

4. **Configure Application Properties**

   Update `src/main/resources/application-dev.yml` with your database credentials:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/finance_db
       username: your_db_username
       password: your_db_password
   ```

5. **Build the Project**

   ```bash
   ./mvnw clean install
   ```

6. **Run the Application**

   ```bash
   ./mvnw spring-boot:run
   ```

   The application will start on `http://localhost:8080`

### Default Admin Account

On first startup, a default admin account is automatically created:

- **Email**: Value from `ADMIN_EMAIL` environment variable
- **Password**: `admin123` (⚠️ Change immediately in production!)
- **Role**: ADMIN

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api/v1
```

### Authentication

#### Login

```http
POST /auth/login
Content-Type: application/json

{
  "userName": "admin@finance.com",
  "password": "admin123"
}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "message": "Login Successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": "user-uuid",
    "name": "Admin User",
    "email": "admin@finance.com",
    "roles": ["ROLE_ADMIN"]
  },
  "timestamp": "2026-04-01T19:04:04.813Z"
}
```

### User Management

All user management endpoints require authentication. Include JWT token in header:

```
Authorization: Bearer <your-jwt-token>
```

#### Create User (Admin Only)

```http
POST /users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "roles": ["ROLE_ANALYST"]
}
```

#### Get All Users (Admin Only)

```http
GET /users
GET /users?status=ACTIVE
```

#### Get User by ID

```http
GET /users/{userId}
```

#### Update User (Admin Only)

```http
PATCH /users/{userId}
Content-Type: application/json

{
  "name": "John Smith",
  "email": "john.smith@example.com"
}
```

#### Activate/Deactivate User (Admin Only)

```http
PATCH /users/{userId}/activate
PATCH /users/{userId}/deactivate
```

#### Assign Role (Admin Only)

```http
POST /users/{userId}/roles
Content-Type: application/json

{
  "roleName": "ROLE_ANALYST"
}
```

#### Remove Role (Admin Only)

```http
DELETE /users/{userId}/roles?role=ROLE_ANALYST
```

### Financial Records

_(APIs under development)_

```http
GET /records                    # List all records (filtered by role)
POST /records                   # Create record (Admin/Analyst)
GET /records/{id}              # Get record details
PATCH /records/{id}            # Update record (Admin)
DELETE /records/{id}           # Soft delete (Admin)
GET /records/filter            # Filter by date, category, type
```

### Dashboard Analytics

_(APIs under development)_

```http
GET /dashboard/summary         # Overview stats
GET /dashboard/trends          # Time-based trends
GET /dashboard/categories      # Category breakdown
GET /dashboard/recent          # Recent transactions
```

### Response Format

All API responses follow a standardized format:

**Success Response:**

```json
{
  "status": "SUCCESS",
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-04-01T19:04:04.813Z"
}
```

**Error Response:**

```json
{
  "status": "FAILURE",
  "message": "Error description",
  "data": null,
  "timestamp": "2026-04-01T19:04:04.813Z"
}
```

## 🗄 Database Schema

### Users Table

```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### User Roles Table (Many-to-Many)

```sql
CREATE TABLE user_roles (
    user_id VARCHAR(255) REFERENCES users(id),
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);
```

### Financial Records Table

```sql
CREATE TABLE financial_records (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) REFERENCES users(id),
    category_id VARCHAR(255) REFERENCES categories(id),
    amount DECIMAL(15, 2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    description VARCHAR(500),
    record_date DATE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Categories Table

```sql
CREATE TABLE categories (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);
```

### Audit Logs Table

```sql
CREATE TABLE audit_logs (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL
);
```

## 🔐 Role-Based Access Control

### Role Hierarchy

| Role        | Permissions                                                                                                          |
| ----------- | -------------------------------------------------------------------------------------------------------------------- |
| **VIEWER**  | - View dashboard summaries<br>- View own profile                                                                     |
| **ANALYST** | - All VIEWER permissions<br>- View all financial records<br>- Access detailed analytics<br>- Export reports          |
| **ADMIN**   | - All ANALYST permissions<br>- Create/Update/Delete records<br>- Manage users<br>- Assign roles<br>- View audit logs |

### Implementation

Access control is enforced at multiple levels:

1. **Method-Level Security**: Using `@PreAuthorize` annotations

   ```java
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<UserResponse> createUser(UserRequestDTO request) { ... }
   ```

2. **JWT Filter**: Validates tokens and sets authentication context

3. **Custom Access Denied Handler**: Returns user-friendly error messages

4. **Database-Level**: Records are associated with users for data isolation

## 🔒 Security

### Authentication Flow

1. User submits credentials to `/auth/login`
2. Server validates credentials against database
3. On success, JWT token is generated and returned
4. Client includes token in `Authorization` header for subsequent requests
5. `JwtFilter` validates token and extracts user details
6. Spring Security enforces role-based access

### Password Security

- Passwords are hashed using BCrypt (work factor: 10)
- Plain text passwords are never stored
- Password validation enforced on registration

### Token Security

- JWT tokens signed with HMAC-SHA256
- Token expiration: 3 hours (configurable)
- Secret key stored in environment variables

### Protection Mechanisms

- SQL Injection: Prevented by JPA/Hibernate parameterized queries
- XSS: Input validation and sanitization
- CSRF: Stateless JWT approach eliminates CSRF risk
- Brute Force: Account lockout on multiple failed attempts (planned)

## 🚨 Error Handling

### Global Exception Handler

Centralized exception handling using `@RestControllerAdvice`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handles ResourceNotFoundException
    // Handles BadRequestException
    // Handles MethodArgumentNotValidException
    // Handles AccessDeniedException
    // Handles generic exceptions
}
```

### Custom Exceptions

- `ResourceNotFoundException`: HTTP 404 for missing resources
- `BadRequestException`: HTTP 400 for invalid input
- Standard validation errors return detailed field-level errors

### Example Error Response

```json
{
  "status": "FAILURE",
  "message": "User not found with id: abc123",
  "data": null,
  "timestamp": "2026-04-01T19:04:04.813Z"
}
```

## 💡 Assumptions & Design Decisions

### Assumptions Made

1. **Single Currency**: All amounts are in one currency (no multi-currency support)
2. **Timezone**: All timestamps are in UTC
3. **User Email as Username**: Email serves as the unique identifier for login
4. **Soft Delete**: Financial records are soft-deleted for audit trail
5. **Admin Self-Service**: Admins can manage their own accounts

### Design Decisions

1. **BigDecimal for Money**: Using `BigDecimal` instead of `float`/`double` to avoid precision errors in financial calculations

2. **UUID as Primary Keys**: Using UUIDs for better distributed system compatibility and security

3. **JWT over Sessions**: Stateless authentication for better scalability

4. **Soft Delete**: Records marked as deleted rather than removed to maintain data integrity and audit history

5. **Separate Record Date**: Transaction date separate from `createdAt` for accurate financial reporting

6. **Enum for Types**: Using enums (`Role`, `UserStatus`, `RecordType`) for type safety and validation

7. **DTOs**: Separate request/response objects to decouple API contracts from domain models

8. **Method Security**: Using `@PreAuthorize` for declarative, readable access control

9. **Audit Logging**: AOP-based auditing to avoid code duplication

10. **Profile-Based Configuration**: Separate configs for dev, staging, and production environments

## 🔮 Future Enhancements

### Planned Features

- [ ] **Financial Records API**: Complete CRUD operations for financial records
- [ ] **Dashboard Analytics**: Summary and trend APIs
- [ ] **Search & Filtering**: Advanced search with multiple criteria
- [ ] **Pagination**: Page-based responses for large datasets
- [ ] **Export Functionality**: CSV/PDF export of records
- [ ] **Bulk Operations**: Import multiple records via CSV
- [ ] **Rate Limiting**: Prevent API abuse
- [ ] **Refresh Tokens**: Long-lived sessions with refresh mechanism
- [ ] **Email Notifications**: Alerts for important events
- [ ] **Two-Factor Authentication**: Enhanced security
- [ ] **API Versioning**: Support multiple API versions
- [ ] **Comprehensive Testing**: Unit and integration tests
- [ ] **API Documentation**: Swagger/OpenAPI integration
- [ ] **Monitoring**: Actuator endpoints for health checks
- [ ] **Caching**: Redis integration for performance
- [ ] **Budget Management**: Set and track budgets by category
- [ ] **Recurring Transactions**: Support for recurring income/expenses
- [ ] **Multi-tenancy**: Support for multiple organizations

### Potential Improvements

- Database indexing for query optimization
- Connection pooling configuration
- Docker containerization
- CI/CD pipeline setup
- Centralized logging (ELK stack)
- Performance monitoring (APM tools)
- Database migration tool (Flyway/Liquibase)

## 📝 License

This project is created for assessment purposes.

## 👤 Author

Developed as part of a backend development assessment to demonstrate:

- RESTful API design
- Spring Boot application architecture
- Role-based access control implementation
- Data modeling and persistence
- Security best practices
- Clean code principles

---

## 🤝 Contributing

This is an assessment project. If you're reviewing this code, feedback is welcome!

---

**Note**: This application is designed for assessment and learning purposes. For production use, additional security hardening, comprehensive testing, and infrastructure setup would be required.
