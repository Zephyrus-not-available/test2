# =====================================================================
# Multi-stage Dockerfile for KTU Voting Application
# Builder: Maven with Eclipse Temurin 21
# Final: Minimal distroless Java 21 image (small runtime)
# =====================================================================

# --- Build stage -------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the application (skip tests for faster builds)
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage (minimal) -------------------------------------------
# Use the official Eclipse Temurin JRE 21 image as a small, supported runtime.
# Distroless Java 21 images may not be available in all registries, so use Temurin.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Default JAR location from the build stage; can be overridden with --build-arg
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} /app/app.jar

# Expose application port
EXPOSE 8080

# Use exec form; distroless images don't include a shell
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
