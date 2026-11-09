# LedgerLine

Secure Financial Transaction Management System built with Spring Boot, PostgreSQL, and Double-Entry Ledger principles.

## Features
- **Double-Entry Bookkeeping**: Atomic multi-leg transfers ensuring zero-sum balance consistency across accounts.
- **Idempotency Protection**: Deterministic idempotency keys preventing duplicate processing of transactions.
- **Role-Based Access Control**: Strict separation between user operations and admin account lifecycle controls (freeze/unfreeze).
- **Immutable Audit Trail**: Append-only log recording actor, action type, entity references, and metadata.
- **Optimistic Concurrency**: Version-controlled account state updates preventing race conditions.

## Tech Stack
- **Backend**: Spring Boot, Spring Data JPA, Spring Security, Flyway
- **Database**: PostgreSQL
- **Security**: JWT Authentication (BCrypt hashing)

## Running the Application
```bash
cd backend
./mvnw spring-boot:run
