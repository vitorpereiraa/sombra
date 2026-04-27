# Sombra

Shadow testing tool for ensuring functional consistency during software architecture revamps.

Sombra captures HTTP traffic from a running service, replays it against a candidate version, compares responses field-by-field, and reports discrepancies — all without modifying application code.

## How It Works

```
  traffic                  Kafka                       HTTP replay
    |                       topic                          |
    v                        |                             v
 System A  ───────────>  [ captured-exchanges ]  ───>  Sombra Server  ───>  System B
 (+ sombra-agent)                                      compare responses
```

1. **Capture** — A servlet filter (`sombra-agent`) intercepts every HTTP request/response on the original service and publishes them to a Kafka topic.
2. **Replay** — `sombra-server` consumes captured exchanges and replays each request against the candidate service.
3. **Compare** — Responses are compared: status codes, headers (optional), and JSON bodies with field-level granularity.
4. **Report** — Discrepancies are logged as structured JSON and exported as metrics via OTLP.

## Modules

| Module | Description |
|--------|-------------|
| `sombra-agent` | Spring Boot starter library added to the original service. Auto-configures a capture filter + Kafka producer. |
| `sombra-server` | Main application. Consumes from Kafka, replays, compares, and reports. |
| `sombra-integration-tests` | End-to-end integration tests using Testcontainers. |
| `sombra-demo` | Two lightweight demo services for live demonstrations. |

## Quick Start

### Prerequisites

- Java 26+
- Docker (for Kafka)

### Build

```bash
./mvnw clean package -DskipTests
```

### Run

```bash
# Start sombra-server (auto-starts Kafka via Docker Compose)
java -jar sombra-server/target/sombra-server-0.0.1-SNAPSHOT-exec.jar --spring.profiles.active=local
```

### Integration Tests

```bash
./mvnw clean verify
```

## Demo

The `sombra-demo` module contains two Spring Boot services that simulate a real shadow testing scenario: a user service being refactored.

- **demo-original** (port 8082) — The "current" service with `sombra-agent` embedded. Every request is transparently captured to Kafka.
- **demo-candidate** (port 8083) — The "new" service. Mostly identical, but with deliberate differences to showcase mismatch detection.

### Running the Demo

Build all modules:

```bash
./mvnw clean package -DskipTests
```

Start the three services in separate terminals:

```bash
# Terminal 1 — candidate service
java -jar sombra-demo/demo-candidate/target/demo-candidate-0.0.1-SNAPSHOT.jar

# Terminal 2 — sombra-server (auto-starts Kafka)
java -jar sombra-server/target/sombra-server-0.0.1-SNAPSHOT-exec.jar --spring.profiles.active=local

# Terminal 3 — original service (with sombra-agent)
java -jar sombra-demo/demo-original/target/demo-original-0.0.1-SNAPSHOT.jar
```

Send requests to the original service:

```bash
curl http://localhost:8082/api/users/1    # Match — identical responses
curl http://localhost:8082/api/users/2    # Mismatch — name, email changed + new field
curl http://localhost:8082/api/users/999  # Mismatch — 404 vs 200 status code
```

Watch the sombra-server terminal for structured comparison logs showing `"match":true/false` with field-level discrepancy details.

### Load Testing with k6

With the demo stack running, use the k6 script to generate sustained load:

```bash
k6 run k6/load-test.js
```

The script cycles through all three user IDs (1, 2, 999) at a constant request rate. Override defaults via environment variables or by editing the top of the script:

```bash
# Custom target URL and run duration
k6 run -e BASE_URL=http://localhost:8082 k6/load-test.js
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `BASE_URL` | `http://localhost:8082` | Target service URL (env var) |
| `USER_IDS` | `[1, 2, 999]` | User IDs to cycle through (edit script) |
| `rate` | `5` | Requests per second (edit script) |
| `duration` | `30s` | Test duration (edit script) |
| `preAllocatedVUs` | `10` | Virtual user pool size (edit script) |

## Configuration

### Agent (added to the original service)

```yaml
sombra:
  agent:
    enabled: true
    topic-name: sombra.captured-exchanges
```

### Server

```yaml
sombra:
  server:
    topic-name: sombra.captured-exchanges
    candidate-url: http://localhost:8083
    comparison:
      ignored-fields: ["/timestamp", "/requestId"]  # optional
      ignore-array-order: true                       # optional
      compare-headers: true                          # optional
      ignored-headers: ["X-Request-Id"]              # optional
```

## Tech Stack

- Spring Boot 4.0.4
- Java 26
- Apache Kafka (KRaft mode)
- Micrometer + OTLP (metrics)
