# ──────────────────────────────
# 1) Build Stage
# ──────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
RUN mvn -B clean package -DskipTests

# ──────────────────────────────
# 2) Runtime Stage
# ──────────────────────────────
FROM eclipse-temurin:17-jre-jammy

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080

# Health check
# Option 1: If Spring Boot Actuator is added, use: CMD curl -f http://localhost:8080/actuator/health || exit 1
# Option 2: Use a simple endpoint check (requires installing curl/wget in the image)
# Option 3: Remove this if health checks are handled externally (e.g., by Kubernetes/Docker Swarm)
# For now, using a simple TCP check (requires netcat or similar - commented out)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#   CMD nc -z localhost 8080 || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]