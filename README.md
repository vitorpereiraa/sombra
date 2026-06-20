# Sombra

Shadow testing tool for ensuring functional consistency during software architecture revamps.

Sombra captures HTTP traffic from a running service, replays it against a candidate version, compares responses field-by-field, and reports discrepancies — all without changing application business logic.

## How It Works

1. **Capture** — A servlet filter (`sombra-agent`) intercepts every HTTP request/response on the original service and publishes them to a Kafka topic.
2. **Replay** — `sombra-server` consumes captured exchanges and replays each request against the candidate service.
3. **Compare** — Responses are compared: status codes, headers (optional), and JSON bodies with field-level granularity.
4. **Report** — Comparison results are logged as structured JSON, and processing metrics are exposed for Prometheus through Spring Boot Actuator.

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
- Docker

### Build

```bash
./mvnw clean package -DskipTests
```

### Run

Two ways to run, depending on whether you're developing `sombra-server`.

Full demo, everything in Docker:

```bash
docker compose up --build
```

IDE/local dev: run `sombra-server` yourself; the `local` profile auto-starts
`compose-local.yaml` (Kafka, demos, observability), which is `compose.yaml` minus
`sombra-server`, so there's no port collision.

```bash
./mvnw -pl sombra-server spring-boot:run -Dspring-boot.run.profiles=local
```

The two modes are mutually exclusive: don't run both at once, since the host ports can
only bind once.

### Integration Tests

```bash
./mvnw clean verify
```

## Demo

The `sombra-demo` module contains two Spring Boot services that simulate a real shadow testing scenario: a user service being refactored.

- **demo-original** (port 8082) — The "current" service with `sombra-agent` embedded. Every request is transparently captured to Kafka.
- **demo-candidate** (port 8083) — The "new" service. Mostly identical, but with deliberate differences to showcase mismatch detection.

### Running the Demo

Start the full local stack:

```bash
docker compose up
```

Docker Compose starts the Kafka broker, `sombra-server`, `demo-original`, `demo-candidate`, Prometheus, Grafana, Loki, and Alloy.

After code changes, rebuild the images before starting the stack:

```bash
docker compose up --build
```

In another terminal, send requests to the original service:

```bash
curl http://localhost:8082/api/users/1    # Match — identical responses
curl http://localhost:8082/api/users/2    # Mismatch — name, email changed + new field
curl http://localhost:8082/api/users/999  # Mismatch — 404 vs 200 status code
```

Watch the `sombra-server` logs for structured comparison events showing `"match":true/false` with field-level discrepancy details.

Useful local endpoints:

| Service | URL |
|---------|-----|
| Sombra server | `http://localhost:8080` |
| Sombra metrics | `http://localhost:8081/actuator/prometheus` |
| Grafana | `http://localhost:3000` |
| Prometheus | `http://localhost:9090` |
| Loki | `http://localhost:3100` |

Stop the demo stack when finished:

```bash
docker compose down
```

### Load Testing with k6

With the demo stack running, use the k6 script to generate sustained load:

```bash
k6 run k6/load-test.js
```

Each iteration requests a random user id and forwards a `divergence` rate, so you can dial how many candidate responses diverge from the original. Both demo apps generate user data deterministically from the id, so a given id always yields the same baseline on both sides; the only discrepancies are the ones the candidate is told to inject.

```bash
# Default: no injected discrepancies
k6 run k6/load-test.js

# Inject discrepancies in ~40% of responses
k6 run -e DISCREPANCY_RATE=0.4 k6/load-test.js

# Custom target URL
k6 run -e BASE_URL=http://localhost:8082 k6/load-test.js
```

The candidate rotates injected discrepancies through every type Sombra reports: value mismatch, field added, field removed, type mismatch, and status mismatch (404 vs 200).

| Parameter | Default | Description |
|-----------|---------|-------------|
| `BASE_URL` | `http://localhost:8082` | Target service URL (env var) |
| `DISCREPANCY_RATE` | `0` | Fraction (0..1) of candidate responses that diverge (env var) |
| `MAX_USER_ID` | `10000` | Upper bound for the random user id per request (env var) |
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
    ingestion:
      topic-name: sombra.captured-exchanges
    replay:
      candidate-url: http://localhost:8083
    comparison:
      ignored-fields: ["/timestamp", "/requestId"]  # optional
      ignore-array-order: true                       # optional
      compare-headers: true                          # optional
      ignored-headers: ["X-Request-Id"]              # optional
```
