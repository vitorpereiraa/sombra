# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sombra is a shadow testing tool built with Spring Boot 4.0.4 (Java 26), part of a Master's thesis on "Ensuring Functional Consistency in Software Through Shadow Testing." It uses Apache Kafka for message brokering and Spring RestClient for HTTP communication.

### Problem and Solution

During architecture revamps (e.g. monolith to microservices), teams need to ensure the new system produces the same functional outcomes as the old one. Sombra performs shadow testing: it consumes request/response pairs from the current version (system A) via a Kafka topic, replays the request against the candidate version (system B) via HTTP, compares both responses, and reports discrepancies.

### Core Workflow

1. **Capture** request/response pairs via an HTTP filter on the current version (sombra-agent)
2. **Consume** those captured exchanges from Kafka (sombra-server)
3. **Replay** the request to the candidate version's configured URL
4. **Compare** responses, respecting configured field ignores
5. **Report** any discrepancies found

## Module Structure

Multi-module Maven project:

- **sombra-agent** — Thin Spring Boot starter library added to system A. Contains an HTTP filter (`CaptureFilter`) that intercepts all requests/responses and publishes them to Kafka as `CapturedExchangeEvent` DTOs. No domain types; extraction from servlets happens in `CapturedExchangeEventMapper`. Auto-configured via `SombraAgentAutoConfiguration`.
- **sombra-server** — The main Spring Boot application. Consumes captured exchanges from Kafka, maps DTOs to strong domain types via `CapturedExchangeMapper`, replays requests to the candidate version via `CandidateReplayService`, and will compare/report them. Owns all domain value objects.
- **sombra-integration-tests** — End-to-end integration tests. Depends on sombra-server. Uses `BaseIT` with `RestTestClient`, `@DynamicPropertySource`, Testcontainers, and maven-failsafe-plugin.

Shared DTOs live in `sombra-agent` (the server depends on the agent for these classes). The agent's autoconfiguration is disabled in the server via `sombra.agent.enabled: false`.

### Package Conventions

**sombra-agent:**
- `service/` — HTTP filter (`CaptureFilter`, skips requests with `X-Sombra-Replay` header)
- `streaming/` — Kafka producer (`CapturedExchangeProducer`) and servlet-to-DTO mapper (`CapturedExchangeEventMapper`)
- `streaming/dto/` — Kafka message shapes (`CapturedExchangeEvent`, `HttpRequestEvent`, `HttpResponseEvent`)

**sombra-server:**
- `domain/` — Core value objects (`CapturedExchange`, `HttpRequest`, `HttpResponse`, `HttpHeader`, `HttpBody`, `HttpMethod`, `StatusCode`, `RequestPath`, `TraceId`)
- `service/` — Replay logic (`CandidateReplayService`) and Spring-to-domain mapper (`ReplayMapper`)
- `streaming/` — Kafka consumer (`CapturedExchangeConsumer`) and DTO-to-domain mapper (`CapturedExchangeMapper`)

## Design Approach

When exploring design alternatives, challenge underlying assumptions before settling on a solution. Don't just compare the obvious options; question whether the framing itself is correct. Use decision analysis (e.g., dominance tables) to compare approaches systematically across dimensions like type safety, simplicity, extensibility, and format-agnosticism.

## Code Style

- **Never use fully qualified class paths** — always add a static or regular import. Double-check generated code for inline references like `java.util.Arrays.stream()`.
- **Never use wildcard imports** (e.g., `import org.springframework.web.bind.annotation.*;`) — always use explicit imports.
- **Use `getFirst()` instead of `get(0)`** on lists — prefer the modern Java API.
- **Use method references over lambdas for single-method calls** — prefer `.map(StaffMemberCountryCode::value)` over `.map(cc -> cc.value())`.
- **Avoid creating private methods** — if a method needs a private helper, push the logic into a domain object, a dedicated component, or inline it. Do not apply DRY reflexively.
- **Use records for `@ConfigurationProperties`** — Spring Boot 4+ supports record-based config properties natively.

## Domain Design

- **Domain types live in sombra-server only** — the agent is a thin capture library with just DTOs.
- **Always create domain value objects for validated data** — don't pass raw strings/primitives through service and streaming layers when validation is needed.
- **The domain is the single source of truth for all rules and invariants** — all validation lives in the domain layer and nowhere else. Other layers trust domain invariants.
- **Never validate in services or streaming layers what domain types already validate** — domain value objects validate their own inputs via their constructor. Other layers call these directly and let them throw `IllegalArgumentException`.
- **Use `checkArgument(x != null, ...)` for null checks** — throws `IllegalArgumentException`. Reserve `checkNotNull` only for `Optional` fields (those should truly never be null).
- **Use `Optional<T>` for optional fields in domain records** — never use nullable fields.
- **Domain models are framework-agnostic** — use Guava Preconditions for validation, not Spring/Jakarta annotations.
- **Value objects**: Java records with `checkArgument()` validation. For `of()` factories returning `Optional`, use try-catch instead of duplicating the constructor's validation logic.
- **Conversion logic belongs in mapper classes** — mappers in the `streaming` package convert between DTOs and domain types, and `ReplayMapper` in the `service` package converts between Spring HTTP types and domain types. Domain types should not reference DTOs or framework types.

## Build Commands

Uses Maven Wrapper (no global Maven install required):

```bash
./mvnw clean verify                                # Full build with integration tests
./mvnw -pl sombra-integration-tests verify         # Run integration tests only
./mvnw -pl sombra-server spring-boot:run -Dspring-boot.run.profiles=local  # Run server locally
./mvnw spotless:check                              # Verify formatting (unused imports)
./mvnw spotless:apply                              # Fix formatting
```

Docker Compose manages Kafka locally (`compose.yaml` at project root) and is started automatically by Spring Boot when using the local profile.

## Architecture

- **Entry point**: `com.github.vitorpereiraa.sombra.SombraApplication` (in sombra-server)
- **Message broker**: Apache Kafka (KRaft mode, single node for dev)
- **Concurrency**: Virtual threads enabled
- **HTTP**: Spring RestClient for outbound replay calls (configured with candidate base URL in `RestClientConfiguration`), Spring MVC for inbound
- **Observability**: Actuator endpoints on port 8081, health and info exposed

## Configuration

All configuration is required with no defaults. Validated with Guava `checkArgument` in `@ConfigurationProperties` records.

- `sombra-server/src/main/resources/application.yaml` — Production config (server on 8080, actuator on 8081, Kafka via `KAFKA_BOOTSTRAP_SERVERS` env var, topic via `SOMBRA_TOPIC_NAME` env var)
- `sombra-server/src/main/resources/application-local.yaml` — Local dev overrides (enables Docker Compose lifecycle, Kafka on localhost:9092, topic name set)

Agent configuration (in the host app that includes sombra-agent):
```yaml
sombra:
  agent:
    enabled: true              # required
    topic-name: my-topic       # required, no default
```

Server configuration:
```yaml
sombra:
  server:
    topic-name: my-topic       # required, no default
    candidate-url: http://system-b:8080  # required, no default
```

## Testing

**Integration tests only** — no unit tests. All tests live in `sombra-integration-tests` module.

- Tests use `*IT` suffix and run via `maven-failsafe-plugin` during `mvn verify`
- `BaseIT` base class provides:
  - `@SpringBootTest` with `DEFINED_PORT` (port found via `findFreePort()` before context starts)
  - `RestTestClient` for HTTP calls (not MockMvc)
  - `@DynamicPropertySource` for all config (no application-test.yaml), including `server.port` and `sombra.server.candidate-url` pointing to the same port
- `TestcontainersConfiguration` provides a `@ServiceConnection` Kafka container with `apache/kafka-native`
- `EchoController` is a test `@RestController` that echoes request bodies and tracks call counts
- E2E tests verify the full flow: HTTP call → CaptureFilter → Kafka → CapturedExchangeConsumer → CandidateReplayService → EchoController
- Use AssertJ for assertions, Awaitility for async verification

## TODOs

- `ResponseComparisonService.lastResult` is temporary scaffolding so integration tests can observe comparison results. Remove it once comparison reporting/persistence is implemented.

## Git Commits and PRs

Never include `Co-Authored-By`, signing, or any attribution lines in commit messages or PR descriptions. Keep commit messages and PR descriptions short and simple. Do not include a test plan section in PR descriptions.
