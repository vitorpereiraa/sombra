FROM eclipse-temurin:26-jdk AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY sombra-agent/pom.xml sombra-agent/
COPY sombra-server/pom.xml sombra-server/
COPY sombra-integration-tests/pom.xml sombra-integration-tests/
COPY sombra-demo/pom.xml sombra-demo/
COPY sombra-demo/demo-original/pom.xml sombra-demo/demo-original/
COPY sombra-demo/demo-candidate/pom.xml sombra-demo/demo-candidate/
RUN ./mvnw -pl sombra-server -am dependency:go-offline -DskipTests -B
COPY sombra-agent/src sombra-agent/src
COPY sombra-server/src sombra-server/src
RUN ./mvnw -pl sombra-server -am package -DskipTests -B

FROM eclipse-temurin:26-jre AS runtime
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /build/sombra-server/target/sombra-server-*-exec.jar /app/sombra-server.jar
EXPOSE 8080 8081
HEALTHCHECK --interval=10s --timeout=3s --retries=10 \
  CMD curl -fsS http://localhost:8081/actuator/health | grep -q '"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "/app/sombra-server.jar"]
