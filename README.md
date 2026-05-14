# Spring Boot Ecommerce API

A portfolio-ready REST API for an ecommerce backend built with **Java 21**, **Spring Boot**, **PostgreSQL**, **Spring Security**, **JWT authentication**, **role-based authorization**, **Swagger/OpenAPI**, and **Azure DevOps CI**.

This project demonstrates a layered backend architecture for products, categories, users, authentication, carts, addresses, orders, payment status handling, and automated service-layer testing.

---

## Features

### Product and Category Management

* Public product browsing
* Public category browsing
* Product-to-category relationship
* Admin-only product creation, update, and deletion
* Admin-only category creation, update, and deletion
* DTO-based request/response design
* Validation for product and category input

### Authentication and Authorization

* User registration
* User login
* JWT token generation
* JWT authentication filter
* Protected current-user endpoint
* Role-based authorization with `ROLE_USER` and `ROLE_ADMIN`
* Admin-only protected operations

### Cart

* Authenticated user cart
* Add product to cart
* Update cart item quantity
* Remove cart item
* Clear cart
* Stock validation before adding/updating cart items
* Server-side subtotal calculation

### Address Management

* Authenticated shipping address management
* Create, view, update, and delete addresses
* Set default address
* Prevent users from accessing other users’ addresses

### Orders

* Create order from authenticated user’s cart
* Use selected shipping address
* Store shipping address snapshot at checkout
* Store order item snapshots including product name and price
* Calculate order total on the server
* Clear cart after order creation
* User order history
* Admin order viewing

### Payment Status Handling

* Orders start as unpaid and pending
* Mark user order as paid
* Store payment method
* Store payment transaction ID
* Store paid timestamp
* Reduce product stock after payment
* Admin order status updates
* Prevent unpaid orders from being marked delivered

### Testing

* Service-layer unit tests with JUnit and Mockito
* Mocked repositories for isolated business logic testing
* Tests for success paths and important failure paths
* Maven test execution included in Azure DevOps pipeline

---

## Tech Stack

| Area                  | Technology                     |
| --------------------- | ------------------------------ |
| Language              | Java 21                        |
| Framework             | Spring Boot 3.5.14             |
| Build Tool            | Maven                          |
| Database              | PostgreSQL                     |
| Persistence           | Spring Data JPA / Hibernate    |
| Security              | Spring Security                |
| Authentication        | JWT                            |
| Validation            | Jakarta Bean Validation        |
| Boilerplate Reduction | Lombok                         |
| API Documentation     | SpringDoc OpenAPI / Swagger UI |
| Testing               | JUnit 5, Mockito, AssertJ      |
| CI                    | Azure DevOps Pipeline          |

---

## Architecture

The project follows a layered Spring Boot architecture:

```text
Controller → Service → Repository → Database
```

### Controller Layer

Handles HTTP requests and responses. Controllers receive DTOs, apply validation, and delegate business logic to services.

### Service Layer

Contains the main business rules, such as:

* Preventing duplicate categories
* Validating product stock
* Creating orders from carts
* Reducing stock after payment
* Preventing duplicate payments
* Enforcing order status rules

### Repository Layer

Uses Spring Data JPA repositories to interact with PostgreSQL.

### Model Layer

Defines JPA entities such as:

* Product
* Category
* AppUser
* Role
* CartItem
* Address
* CustomerOrder
* OrderItem

---

## API Overview

### Authentication

| Method | Endpoint             | Access        | Description              |
| ------ | -------------------- | ------------- | ------------------------ |
| POST   | `/api/auth/register` | Public        | Register a new user      |
| POST   | `/api/auth/login`    | Public        | Login and receive JWT    |
| GET    | `/api/auth/me`       | Authenticated | Get current user profile |

### Products

| Method | Endpoint             | Access | Description       |
| ------ | -------------------- | ------ | ----------------- |
| GET    | `/api/products`      | Public | Get all products  |
| GET    | `/api/products/{id}` | Public | Get product by ID |
| POST   | `/api/products`      | Admin  | Create product    |
| PUT    | `/api/products/{id}` | Admin  | Update product    |
| DELETE | `/api/products/{id}` | Admin  | Delete product    |

### Categories

| Method | Endpoint               | Access | Description        |
| ------ | ---------------------- | ------ | ------------------ |
| GET    | `/api/categories`      | Public | Get all categories |
| GET    | `/api/categories/{id}` | Public | Get category by ID |
| POST   | `/api/categories`      | Admin  | Create category    |
| PUT    | `/api/categories/{id}` | Admin  | Update category    |
| DELETE | `/api/categories/{id}` | Admin  | Delete category    |

### Cart

| Method | Endpoint                      | Access        | Description               |
| ------ | ----------------------------- | ------------- | ------------------------- |
| GET    | `/api/cart`                   | Authenticated | Get current user’s cart   |
| POST   | `/api/cart/items`             | Authenticated | Add item to cart          |
| PUT    | `/api/cart/items/{productId}` | Authenticated | Update cart item quantity |
| DELETE | `/api/cart/items/{productId}` | Authenticated | Remove item from cart     |
| DELETE | `/api/cart`                   | Authenticated | Clear cart                |

### Addresses

| Method | Endpoint                      | Access        | Description                  |
| ------ | ----------------------------- | ------------- | ---------------------------- |
| POST   | `/api/addresses`              | Authenticated | Create address               |
| GET    | `/api/addresses`              | Authenticated | Get current user’s addresses |
| GET    | `/api/addresses/{id}`         | Authenticated | Get address by ID            |
| PUT    | `/api/addresses/{id}`         | Authenticated | Update address               |
| PATCH  | `/api/addresses/{id}/default` | Authenticated | Set default address          |
| DELETE | `/api/addresses/{id}`         | Authenticated | Delete address               |

### Orders

| Method | Endpoint                             | Access        | Description                    |
| ------ | ------------------------------------ | ------------- | ------------------------------ |
| POST   | `/api/orders`                        | Authenticated | Create order from cart         |
| GET    | `/api/orders/my`                     | Authenticated | Get current user’s orders      |
| GET    | `/api/orders/my/{orderId}`           | Authenticated | Get current user’s order by ID |
| PATCH  | `/api/orders/my/{orderId}/pay`       | Authenticated | Mark order as paid             |
| GET    | `/api/orders/admin`                  | Admin         | Get all orders                 |
| GET    | `/api/orders/admin/{orderId}`        | Admin         | Get order by ID                |
| PATCH  | `/api/orders/admin/{orderId}/status` | Admin         | Update order status            |

---

## Security Model

The API uses JWT-based stateless authentication.

Public endpoints include:

```text
POST /api/auth/register
POST /api/auth/login
GET /api/products/**
GET /api/categories/**
```

Authenticated user endpoints include:

```text
GET /api/auth/me
/api/cart/**
/api/addresses/**
/api/orders
/api/orders/my/**
/api/orders/my/{orderId}/pay
```

Admin-only endpoints include:

```text
POST /api/products/**
PUT /api/products/**
DELETE /api/products/**

POST /api/categories/**
PUT /api/categories/**
DELETE /api/categories/**

/api/orders/admin/**
```

---

## Local Setup

### Prerequisites

* Java 21
* Maven Wrapper included in the project
* PostgreSQL
* IntelliJ IDEA or another Java IDE

### Clone the repository

```bash
git clone https://github.com/percyosag/springboot-ecommerce-api.git
cd springboot-ecommerce-api
```

### Configure PostgreSQL

Create a local PostgreSQL database, then configure the database connection in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/springboot_ecommerce_api}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
```
Set `SPRING_DATASOURCE_PASSWORD` in your local environment or IntelliJ run configuration instead of committing a real password.

For JWT configuration:

```properties
app.jwt.secret=${JWT_SECRET:dev-secret-key-must-be-at-least-32-characters-long-for-hs256}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
```


For optional local admin seeding:

```properties
app.admin.seed-enabled=${APP_ADMIN_SEED_ENABLED:false}
app.admin.username=${APP_ADMIN_USERNAME:admin}
app.admin.email=${APP_ADMIN_EMAIL:admin@example.com}
app.admin.password=${APP_ADMIN_PASSWORD:}
```

In IntelliJ local run configuration, you can set:

```text
APP_ADMIN_SEED_ENABLED=true;APP_ADMIN_PASSWORD=your_local_admin_password
```

Do not commit real production secrets.

---

## Run the Application

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Or run the main application class from IntelliJ.

The API will start at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Run Tests

Run all tests:

```powershell
.\mvnw.cmd clean test
```

Run a specific test class:

```powershell
.\mvnw.cmd -Dtest=CategoryServiceImplTest test
```

Run selected test classes in PowerShell:

```powershell
.\mvnw.cmd "-Dtest=CategoryServiceImplTest,ProductServiceImplTest,CartServiceImplTest,OrderServiceImplTest,AuthServiceImplTest" test
```

---

## Testing Strategy

This project uses service-layer unit tests for the most important business rules.

Tested areas include:

* Category creation and duplicate prevention
* Product creation with category validation
* Cart item creation, quantity merging, and stock validation
* Order creation from cart
* Payment status update and stock reduction
* Admin order status rules
* User registration and login service logic

The tests use Mockito to mock repositories and security dependencies so business logic can be tested without starting the full application or connecting to PostgreSQL.

---

## Azure DevOps CI

The project includes an Azure DevOps pipeline configuration:

```text
azure-pipelines.yml
```

The pipeline runs Maven build and tests to verify that changes pass before continuing to the next milestone.

---

## Key Learning Outcomes

This project demonstrates:

* Layered Spring Boot API architecture
* DTO-based API design
* Validation and clean error responses
* PostgreSQL persistence with JPA
* JWT authentication
* Role-based authorization
* User-owned resources
* Cart and checkout workflow
* Order snapshot design
* Payment status handling
* Service-layer unit testing
* Swagger/OpenAPI documentation
* Azure DevOps CI workflow

---

## Future Improvements

Possible future enhancements:

* Add integration tests with Testcontainers
* Add real payment provider integration such as Stripe or PayPal
* Add pagination and filtering for products and orders
* Add refresh tokens
* Add deployment configuration
* Add Docker Compose for local PostgreSQL setup
* Add production profile configuration
