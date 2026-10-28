# Calculator REST API — End-to-End CI/CD Pipeline

## 1. Overview

This project implements a RESTful Calculator API using Java, Spring Boot, and Maven, supported by an automated Continuous Integration and Continuous Deployment (CI/CD) pipeline.

The pipeline uses Jenkins to build and test the application, build and publish Docker images, deploy the application using Docker Compose, verify deployment health, and send email notifications for successful and failed builds.

**Key features**
- REST API supporting addition, subtraction, multiplication, and division.
- Input validation and structured error responses.
- Automated unit and integration testing.
- Multi-stage Docker image build.
- Automated Docker Hub image publishing.
- Multi-container deployment using Docker Compose and Nginx.
- Health checks and restart policies.
- GitHub webhook integration with Jenkins.
- Email notifications for build success and failure.

### Project Links

- **GitHub Repository:** https://github.com/BookwormBoy/calculator_devops
- **Docker Hub Image:** https://hub.docker.com/r/skini5001/calculator_devops

## 2. Architecture

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
              +-------+-------+
              |               |
          Maven Build     Maven Tests
              |               |
              +-------+-------+
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
          Internal 8081     Host 8082
                              |
                              v
                       External Clients

                Jenkins Email Notifications
```

### Request Flow

1. A developer pushes changes to the GitHub repository.
2. A GitHub webhook triggers the Jenkins pipeline.
3. Jenkins checks out the source code, builds the application, and runs automated tests.
4. Jenkins builds a Docker image and pushes it to Docker Hub.
5. Docker Compose deploys the calculator image alongside Nginx.
6. Nginx forwards incoming HTTP requests to the calculator service.
7. Jenkins verifies application health and sends an email notification with the build outcome.

## 3. Technology Stack

| Component | Technology |
|---|---|
| Programming language | Java |
| Application framework | Spring Boot |
| Build tool | Apache Maven |
| Unit testing | JUnit 5 |
| Integration testing | Spring Boot Test and MockMvc |
| Version control | Git and GitHub |
| CI/CD | Jenkins |
| Containerization | Docker |
| Container orchestration | Docker Compose |
| Reverse proxy | Nginx |
| Image registry | Docker Hub |

## 4. REST API

The application exposes the following endpoints.

| Operation | HTTP Method | Endpoint | Description |
|---|---|---|---|
| Addition | GET | `/api/v1/add?a=10&b=5` | Returns the sum |
| Subtraction | GET | `/api/v1/subtract?a=10&b=5` | Returns the difference |
| Multiplication | GET | `/api/v1/multiply?a=10&b=5` | Returns the product |
| Division | GET | `/api/v1/divide?a=10&b=5` | Returns the quotient |
| Health | GET | `/actuator/health` | Reports application health |

The calculator listens on port `8081` inside the container. Nginx exposes the application through port `8082` on the host.

### Example Requests

Once the Docker Compose deployment is running, use the following commands:

```bash
curl "http://localhost:8082/api/v1/add?a=10&b=5"
curl "http://localhost:8082/api/v1/subtract?a=10&b=5"
curl "http://localhost:8082/api/v1/multiply?a=10&b=5"
curl "http://localhost:8082/api/v1/divide?a=10&b=5"
```

### Example Successful Response

For addition with `a=10` and `b=5`, the expected response structure is:

```json
{
  "operation": "add",
  "a": 10,
  "b": 5,
  "result": 15
}
```

### Input Validation and Error Handling

The API validates its input parameters and returns HTTP `400 Bad Request` with a structured JSON error response for invalid requests.

Error conditions include:

- Missing operands.
- Non-numeric operands.
- Division by zero.

For example, division by zero can be tested with:

```bash
curl -i "http://localhost:8082/api/v1/divide?a=10&b=0"
```

The response should have HTTP status `400` and a JSON error body describing the problem.

The exact error fields and messages are determined by the application's exception handling implementation.

## 5. Application Testing

The application uses JUnit 5 and Spring Boot Test to verify its functionality.

### Unit Tests

Unit tests cover the calculator operations:

- Addition.
- Subtraction.
- Multiplication.
- Division.
- Division by zero.

### Integration Testing

A Spring MVC integration test uses MockMvc to exercise an actual API endpoint through the Spring MVC request-handling layer.

### Running Tests

From the project directory, run:

```bash
mvn test
```

To compile, package, and test the application, run:

```bash
mvn clean package
```

Maven produces test reports in:

```text
target/surefire-reports/
```

The Jenkins pipeline publishes the generated JUnit XML reports so that test results are visible in the Jenkins interface.

A test failure causes the Test stage to fail, preventing subsequent pipeline stages from executing successfully.

## 6. Docker Containerization

The application is containerized using a multi-stage Dockerfile.

### Multi-Stage Build

The Dockerfile separates application compilation from runtime execution:

1. **Build stage:** Uses a Maven/JDK image to compile the source code and package the executable JAR.
2. **Runtime stage:** Uses a slim JRE image and copies the packaged JAR from the build stage.

This avoids including the complete Maven build environment in the final runtime image.

### Docker Best Practices

The containerization configuration is designed around the following practices:

- Multi-stage builds to separate compilation and execution.
- A minimal JRE runtime image.
- Dependency-layer caching to avoid unnecessarily downloading dependencies on every build.
- A non-root runtime user to reduce privileges.
- An explicit exposed application port.
- A `.dockerignore` file to exclude unnecessary files from the Docker build context.

### Building and Running a Standalone Container

Build a local image:

```bash
docker build -t calculator:local .
```

Run the container:

```bash
docker run --rm -p 8081:8081 calculator:local
```

The application should then be accessible at:

```text
http://localhost:8081
```

Check its health using:

```bash
curl http://localhost:8081/actuator/health
```

The standalone container can be stopped with `Ctrl+C`.

## 7. Docker Compose Deployment

Docker Compose orchestrates the calculator API and Nginx as separate services.

### Calculator Service

The calculator service:

- Uses the published Docker Hub image.
- Listens on port `8081` inside the container.
- Exposes a health check through `/actuator/health`.
- Uses a restart policy to recover from unexpected container termination.
- Connects to the shared Compose network.

### Nginx Service

Nginx acts as a reverse proxy in front of the calculator API.

- Exposes port `8082` on the host.
- Forwards incoming requests to the calculator service on port `8081`.
- Shares the Compose network with the calculator service.
- Starts after the calculator service becomes healthy.

### Running the Full Stack

Clone the repository:

```bash
git clone https://github.com/BookwormBoy/calculator_devops.git
cd calculator_devops
```

Pull the published calculator image and start the services:

```bash
docker compose pull
docker compose up -d
```

Docker Compose starts the calculator API and Nginx together.

Check the running services:

```bash
docker compose ps
```

Check the application health:

```bash
curl http://localhost:8082/actuator/health
```

Stop the deployment:

```bash
docker compose down
```

### Repeatable Deployment

The Jenkins pipeline deploys the image associated with the current build number.

It pulls the published image and uses Docker Compose to update the running services. The health-verification stage checks whether the application is reachable before declaring the pipeline successful.

Docker Compose manages the service containers and network, allowing the deployment to be repeated without manually creating containers or networks.

## 8. Jenkins CI/CD Pipeline

The pipeline is defined declaratively in the `Jenkinsfile` stored in the GitHub repository.

### Pipeline Stages

| Stage | Purpose |
|---|---|
| Checkout | Retrieves the latest source code from GitHub. |
| Build | Runs `mvn clean package -DskipTests` to compile and package the application. |
| Test | Runs `mvn test` and publishes JUnit XML reports. |
| Docker Build | Builds the application image and tags it with the Jenkins build number and `latest`. |
| Push | Authenticates with Docker Hub using Jenkins-managed credentials and pushes both image tags. |
| Deploy | Pulls the image corresponding to the current build number and deploys the services using Docker Compose. |
| Verify Health | Checks the application health endpoint and retries before marking deployment verification as failed. |

### Automatic Triggering

A GitHub webhook triggers Jenkins when changes are pushed to the configured branch.

This enables the CI/CD workflow to execute automatically without requiring the developer to start every build manually.

### Docker Image Versioning

Images are tagged using the Jenkins build number and the `latest` tag.

For example:

```text
skini5001/calculator_devops:<BUILD_NUMBER>
skini5001/calculator_devops:latest
```

The numbered tag identifies a particular pipeline build, while `latest` points to the most recently published image.

The deployment stage uses the numbered tag to deploy the image produced by that pipeline run.

### Deployment Health Verification

The pipeline checks:

```text
http://localhost:8082/actuator/health
```

The health-verification stage retries the request if the application is not immediately available. If health verification fails after the retries, the pipeline fails rather than reporting a successful deployment.

### Email Notifications

The Jenkins pipeline sends email notifications for both successful and failed builds.

Email delivery is configured using the Jenkins Extended E-mail Notification settings. SMTP credentials are configured in Jenkins rather than committed to the repository.

## 9. Version Control and Security

The project uses Git and GitHub for source control and CI/CD integration.

The development workflow includes:

- A dedicated feature branch for the CI/CD implementation.
- A pull request or equivalent merge process into `main`.
- Progressive commits documenting the implementation.
- A public GitHub repository containing the application and pipeline configuration.

### Credential Management

Credentials are managed outside the source code:

- Docker Hub credentials are stored in Jenkins Credentials.
- SMTP credentials are configured in Jenkins.
- Gmail App Passwords and other secrets must not be committed to GitHub.

The `.gitignore` file should exclude generated build output, IDE-specific files, and local configuration or secret files. The `.dockerignore` file should exclude unnecessary files from the Docker build context.

## 10. Project Structure

```text
calculator_devops/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/example/calculator/
│   └── test/
├── nginx/
│   └── default.conf
├── Dockerfile
├── .dockerignore
├── .gitignore
├── docker-compose.yml
├── Jenkinsfile
├── pom.xml
└── README.md
```

## 11. Conclusion

This project demonstrates an end-to-end CI/CD workflow for a Spring Boot REST API.

By integrating GitHub, Jenkins, Maven, Docker, Docker Hub, Docker Compose, and Nginx, it automates application building, testing, container image publishing, deployment, health verification, and email notifications.

The resulting workflow demonstrates automated software delivery, container orchestration, application health monitoring, and repeatable deployment using the tools covered in the Software Production Engineering course.