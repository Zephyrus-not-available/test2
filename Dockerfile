# =====================================================================
# Multi-stage Dockerfile for KTU Voting Application
# Builder: Maven with Eclipse Temurin 21
# Final: Minimal Java 21 runtime
# =====================================================================

# --- Build stage -------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom first for caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build (skip tests)
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage -----------------------------------------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy generated JAR from build stage
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} /app/app.jar

# Expose app port
EXPOSE 8080

# IMPORTANT: Activate prod profile for Render
ENV SPRING_PROFILES_ACTIVE=prod

# Run Spring Boot with port provided by Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
