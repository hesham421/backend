# erp-backend — built ON THE SERVER (x86_64) by deploy.py.
# Build context: ~/erp-deploy/backend (pom.xml + src only).
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget curl && \
    addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    mkdir -p /app/logs && \
    chown -R appuser:appgroup /app
COPY --from=build --chown=appuser:appgroup /app/target/erp-system-*.jar app.jar
USER appuser
EXPOSE 7272
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
