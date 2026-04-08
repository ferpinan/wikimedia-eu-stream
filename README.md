# Wikimedia EU Stream Filter

A Spring Boot and Kafka Streams application designed for real-time processing of Wikimedia recent changes event streams. Its primary objective is to consume the global stream, filter events to isolate a specific language (defaulting to the Basque Wikipedia - `euwiki`), and publish the resulting dataset to a dedicated topic for downstream consumption.

## Core Functionality

1. **Consume:** Ingests raw JSON messages from a source Kafka topic (`wikimedia.recentchange`).
2. **Parse:** Extracts the wiki identifier (`wiki`) in a fault-tolerant manner utilizing Jackson.
3. **Filter:** Processes the stream by discarding any events that do not match the configured language identifier.
4. **Produce:** Forwards the filtered events to a destination topic (`wikimedia.recentchange.eu`).

## Technology Stack

* **Java 21**
* **Spring Boot** (includes Actuator for health and metric monitoring)
* **Apache Kafka Streams**
* **Docker & Docker Compose** (optimized multi-stage build)

## Configuration

The application is highly configurable via environment variables or by modifying the `application.yml` file. The primary parameters are outlined below:

| YAML Property | Environment Variable | Description | Default Value |
| :--- | :--- | :--- | :--- |
| `spring.kafka.bootstrap-servers` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka cluster connection string. | `localhost:9092` |
| `wikimedia.lang-wiki-id` | `WIKIMEDIA_LANG_WIKI_ID` | The specific wiki identifier to filter by. | `euwiki` |
| `wikimedia.source-topic` | `WIKIMEDIA_SOURCE_TOPIC` | Source topic containing the global event stream. | `wikimedia.recentchange` |
| `wikimedia.sink-topic` | `WIKIMEDIA_SINK_TOPIC` | Destination topic for the filtered events. | `wikimedia.recentchange.eu` |

## Execution Instructions

### Prerequisites
* A running **Kafka** cluster (accessible at `localhost:9092` or your configured host).
* **Java 21** and **Maven** (for local execution).
* **Docker** (for containerized deployment).

### Option A: Local Execution (Development)

1. Clone the repository and navigate to the project directory.
2. Compile the project using Maven:
   ```bash
   mvn clean package -DskipTests
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

### Option B: Docker Deployment

The project includes an optimized multi-stage `Dockerfile` and a `docker-compose.yml` configuration.

1. Ensure the required external Docker network is created (as defined in the `docker-compose.yml`):
   ```bash
   docker network create ferpinan-network
   ```
2. Start the container in detached mode:
   ```bash
   docker-compose up -d
   ```
*Note: The provided `docker-compose.yml` assumes a Kafka broker is already running and accessible via the hostname `kafka` within the specified Docker network.*

## Monitoring

The application exposes administration endpoints via Spring Boot Actuator to facilitate monitoring in production environments:

* **Health Check:** `http://localhost:8080/actuator/health` (Displays comprehensive details regarding application health and Kafka connectivity).
* **Metrics:** `http://localhost:8080/actuator/metrics`