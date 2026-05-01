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
RUN ./mvnw -pl sombra-demo/demo-original -am dependency:go-offline -DskipTests -B
COPY sombra-agent/src sombra-agent/src
COPY sombra-demo/demo-original/src sombra-demo/demo-original/src
RUN ./mvnw -pl sombra-demo/demo-original -am package -DskipTests -B

FROM eclipse-temurin:26-jre AS runtime
WORKDIR /app
COPY --from=build /build/sombra-demo/demo-original/target/demo-original-*.jar /app/demo-original.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/demo-original.jar"]
