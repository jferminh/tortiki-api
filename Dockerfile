# syntax=docker/dockerfile:1

# ============================================================
# STAGE 1 — Build Maven (JDK complet, non conservé à l'exécution)
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B --no-transfer-progress

COPY src ./src
RUN ./mvnw clean package -DskipTests -B --no-transfer-progress

# ============================================================
# STAGE 2 — Image d'exécution (JRE minimal, sans outils de build)
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S tortiki && adduser -S tortiki -G tortiki

WORKDIR /app
COPY --from=build /workspace/target/tortiki-api-*.jar app.jar

USER tortiki

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]