# ==============================================================================
# Multi-stage Dockerfile for Spring Boot Application on Render
# ==============================================================================

# Stage 1: Build the JAR with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml and resolve dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build the artifact (skipping tests for fast build)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Ultra-lightweight JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Expose HTTP port (Render will bind to $PORT)
EXPOSE 8080

# Tune JVM heap for Render's 512MB free container memory limit
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Copy compiled jar from build stage
COPY --from=builder /app/target/*.jar app.jar

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
