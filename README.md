# Calculator REST API — End-to-End CI/CD Pipeline

## Overview

This project implements a RESTful calculator API using Java and Spring Boot, with an end-to-end Continuous Integration and Continuous Deployment (CI/CD) pipeline using Jenkins.

The pipeline automatically builds and tests the application, builds and publishes a Docker image to Docker Hub, deploys the application using Docker Compose, verifies deployment health, and sends email notifications for successful and failed builds.

## Architecture

```text
                 Developer
                     |
                     v
              GitHub Repository
                     |
              GitHub Webhook
                     |
                     v
                  Jenkins
                     |
              +------+------+
              |             |
           Maven Build   Unit Tests
              |             |
              +------+------+
                     |
                     v
               Docker Build
                     |
                     v
                Docker Hub
                     |
                     v
             Docker Compose
                     |
             +-------+-------+
             |               |
        Calculator API      Nginx
         (Port 8081)      (Port 8082)
                             |
                        External Clients

         Jenkins Email Notifications
```

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build Tool:** Apache Maven
- **Testing:** JUnit 5, Spring Boot Test
- **CI/CD:** Jenkins
- **Version Control:** Git and GitHub
- **Containerization:** Docker
- **Container Orchestration:** Docker Compose
- **Reverse Proxy:** Nginx
- **Image Registry:** Docker Hub

## REST API

The application provides the following endpoints:

| Operation | HTTP Method | Endpoint | Description |
|---|---|---|---|
| Addition | GET | `/api/v1/add` | Adds two numbers |
| Subtraction | GET | `/api/v1/subtract` | Subtracts one number from another |
| Multiplication | GET | `/api/v1/multiply` | Multiplies two numbers |
| Division | GET | `/api/v1/divide` | Divides one number by another |

The application listens on port `8081` inside its container. Nginx exposes the API through port `8082` on the host.

### Example Requests

Assuming the application is running locally with the Docker Compose deployment:

```bash
curl "http://localhost:8082/api/v1/add?a=10&b=5"
curl "http://localhost:8082/api/v1/subtract?a=10&b=5"
curl "http://localhost:8082/api/v1/multiply?a=10&b=5"
curl "http://localhost:8082/api/v1/divide?a=10&b=5"
```

Replace `a` and `b` with the desired operands. These examples assume the controller uses `a` and `b` as its request parameter names.

## Running Locally

### Prerequisites

- Java Development Kit compatible with the project
- Apache Maven, or the included Maven Wrapper
- Docker Engine
- Docker Compose

### Build and Test

Clone the repository and enter the project directory:

```bash
git clone https://github.com/BookwormBoy/calculator_devops.git
cd calculator
```

Build the application and run the tests:

```bash
./mvnw clean package
./mvnw test
```

Alternatively, use `mvn` if Maven is installed.

### Run with Docker Compose

Build and start the application and Nginx:

```bash
docker compose up -d --build
```

Access the API at:

```text
http://localhost:8082
```

Check the container status:

```bash
docker compose ps
```

Stop the services:

```bash
docker compose down
```

## CI/CD Pipeline

The pipeline is defined in the `Jenkinsfile` and consists of the following stages.

### 1. Checkout

Retrieves the source code from the configured GitHub branch.

### 2. Build

Uses Maven to compile and package the Spring Boot application.

### 3. Test

Executes automated tests using Maven and publishes the JUnit test results in Jenkins.

### 4. Docker Build

Builds a Docker image containing the application and tags it with the Jenkins build number and `latest`.

### 5. Push

Authenticates with Docker Hub using Jenkins-managed credentials and publishes both image tags.

### 6. Deploy

Pulls the image corresponding to the current Jenkins build number and deploys it using Docker Compose.

Docker Compose runs the calculator application and Nginx reverse proxy.

### 7. Verify Health

Checks the application's Spring Boot Actuator health endpoint through Nginx:

```text
http://localhost:8082/actuator/health
```

The pipeline retries the check before marking the deployment as failed.

### Notifications

The pipeline sends email notifications when a build succeeds or fails. Email delivery is configured through Jenkins' Extended E-mail Notification settings.

## Automation

A GitHub webhook triggers Jenkins when changes are pushed to the configured branch. This removes the need to start each pipeline run manually.

The pipeline automates:

- Source checkout
- Application build and testing
- Docker image creation and publishing
- Deployment
- Health verification
- Success and failure notifications

## Configuration and Security

Credentials are managed through Jenkins rather than being stored in the source repository.

- Docker Hub credentials are stored in Jenkins Credentials.
- SMTP credentials are configured in Jenkins for email notifications.
- No passwords, access tokens, or Gmail App Passwords should be committed to GitHub.

## Project Structure

```text
calculator/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/example/calculator/
│   └── test/
├── nginx/
│   └── default.conf
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
├── pom.xml
└── README.md
```

## Conclusion

This project demonstrates an automated software delivery workflow for a REST API. By integrating GitHub, Jenkins, Maven, Docker, Docker Hub, Docker Compose, and Nginx, it automates testing, image publishing, deployment, and health verification while providing email notifications for pipeline outcomes.