# Bank Management Application

![Bank Application](https://raw.githubusercontent.com/BrodyGaudel/bank/refs/heads/main/illustration.jpg)

## Overview
The **Bank Management Application** is a microservices-based system designed to handle various banking operations, such as customer management, account transactions (credit, debit, transfer), authentication, and notifications. The project implements **CQRS** (Command Query Responsibility Segregation) and **Event Sourcing** patterns using the **Axon Framework** for the **Account Service**, ensuring scalability and performance while handling large amounts of banking data.

The frontend of the application is developed using **Angular**, while the backend services are built with **Spring Boot**. All services are containerized with **Docker**.

## Architecture
The system follows a **microservices architecture** and utilizes various Spring Cloud technologies, including **Spring Cloud Eureka Server** for service discovery and **Spring Cloud Reactive Gateway** for routing.

### Microservices

| Service                  | Port  | Description                                                                 |
|--------------------------|-------|-----------------------------------------------------------------------------|
| **Discovery Service**    | 8761  | Eureka Server for service discovery and registration                        |
| **Gateway Service**      | 8888  | Reactive API gateway to route requests to respective microservices          |
| **Customer Service**     | 8886  | Manages customer information within the system                              |
| **Account Service**      | 8884  | Manages bank accounts (credit, debit, transfers) using CQRS/Event Sourcing |
| **Authentication Service** | 8885 | Handles user authentication, role-based access control, and authorization   |
| **Notification Service** | 8887  | Manages email notifications for various banking operations                  |

## Technologies
The project uses a range of technologies to deliver a reliable, scalable, and efficient banking management solution:

- **Java 21**
- **Spring Boot 3.3.4**
- **Axon Framework** with **Axon Server** for CQRS and Event Sourcing
- **Spring Cloud OpenFeign** for inter-service communication
- **MySQL** as the primary database
- **Angular** for the frontend UI
- **Docker** for containerization
- **GitHub Actions** for CI/CD pipeline automation
- **OpenAPI/Swagger** for API documentation

## CI/CD Pipeline

This project uses **GitHub Actions** for continuous integration. The pipeline is defined in `.github/workflows/ci.yml` and runs on every push to `main` and on pull requests targeting `main`.

The CI pipeline performs the following steps:
1. Checks out the code
2. Sets up JDK 21 (Temurin distribution) with Maven caching
3. Runs `mvn clean verify -DskipTests` to compile and package all modules
4. Runs `mvn test` to execute the test suite

## API Documentation (OpenAPI/Swagger)

Each service exposes OpenAPI documentation via Swagger UI. Once the services are running, you can access the Swagger UI at:

- **Customer Service**: [http://localhost:8886/bank/swagger-ui.html](http://localhost:8886/bank/swagger-ui.html)
- **Account Service**: [http://localhost:8884/bank/swagger-ui.html](http://localhost:8884/bank/swagger-ui.html)
- **Authentication Service**: [http://localhost:8885/bank/swagger-ui.html](http://localhost:8885/bank/swagger-ui.html)
- **Gateway (aggregated)**: [http://localhost:8888/swagger-ui.html](http://localhost:8888/swagger-ui.html)

The Gateway Service aggregates the API docs from all downstream services, so you can browse all APIs from a single Swagger UI.

## Service Start-up Order
To ensure proper initialization of services, they need to be started in the following order:

1. **Axon Server**
2. **Discovery Service**
3. **Gateway Service**
4. **Notification Service**
5. **Authentication Service**
6. **Customer Service**
7. **Account Service**

> **Note**: The **Axon Server** should be started first as it handles event storage and routing for the **Account Service**. For more information about Axon Server, please refer to its [official documentation](https://www.axoniq.io/products/axon-server).

## Axon Framework and Axon Server
This application leverages the **Axon Framework** for implementing **CQRS** and **Event Sourcing** in the **Account Service**. Axon Framework provides the tools necessary to build scalable and maintainable event-driven systems. It uses **Axon Server** for event storage and routing.

- Axon Framework Documentation: [Axon Framework](https://www.axoniq.io/products/axon-framework)
- Axon Server Documentation: [Axon Server](https://www.axoniq.io/products/axon-server)

## Getting Started

### Prerequisites
- **Java 21**
- **Maven**
- **Node.js** (for the frontend)
- **MySQL** (configured with the necessary schema)
- **Docker & Docker Compose**
- **Axon Server** (start Axon Server before running the microservices)

### Running with Docker Compose

The easiest way to start all services is using Docker Compose:

```bash
docker compose up --build
```

This will build and start all microservices along with their dependencies. The services will be available on their respective ports as listed in the table above.

To stop all services:

```bash
docker compose down
```

### Backend Setup (Manual)

1. Clone the backend repository:
    ```bash
    git clone https://github.com/BrodyGaudel/bank
    cd bank
    ```

2. Package the microservices with Maven:
    ```bash
    mvn clean install
    ```

3. Start each service individually using Maven:
    - Start the **Axon Server** (make sure it's running before starting any other services):
      ```bash
      java -jar axonserver.jar
      ```

    - Start the **Discovery Service**:
      ```bash
      cd discovery-service
      mvn spring-boot:run
      ```

    - Start the **Gateway Service**:
      ```bash
      cd gateway-service
      mvn spring-boot:run
      ```

    - Follow the same steps for the other services in the order mentioned above.

### Frontend Setup

1. Clone the frontend repository:
    ```bash
    git clone https://github.com/BrodyGaudel/bank-ui
    cd bank-ui
    ```

2. Install dependencies and run the application:
    ```bash
    npm install
    npm start
    ```

3. Access the frontend via `http://localhost:4200`.

## Features

- **Customer Management**: CRUD operations for bank customers.
- **Account Operations**: Handles bank account creation, credits, debits, and transfers.
- **CQRS and Event Sourcing**: The **Account Service** uses Axon Framework for scalable event-driven architecture.
- **Authentication & Authorization**: Role-based access control for bank employees.
- **Email Notifications**: Automated email notifications for customer and account operations.

## Microservices Communication
Inter-service communication is achieved using **Spring Cloud OpenFeign**, enabling synchronous calls between microservices.

## Monitoring & Quality
- **GitHub Actions** is used for CI/CD automation, handling the build and test pipelines.
- **OpenAPI/Swagger** is integrated for interactive API documentation.

## Project Structure

```
bank/
    ├── discovery-service/      (port 8761)
    ├── gateway-service/        (port 8888)
    ├── customer-service/       (port 8886)
    ├── account-service/        (port 8884)
    ├── authentication-service/ (port 8885)
    ├── notification-service/   (port 8887)
    ├── docker-compose.yml
    └── .github/workflows/ci.yml
```

## Contributions
Contributions are welcome! Please feel free to submit a pull request or open an issue for any bugs or feature requests.

## License
This project is licensed under the MIT License.

## Contact
For any inquiries, please contact:
- **Brody Gaudel** - brodymounanga@gmail.com

## Repositories
- **Backend**: [Bank Backend Repository](https://github.com/BrodyGaudel/bank)
- **Frontend**: [Bank Frontend Repository](https://github.com/BrodyGaudel/bank-ui)
