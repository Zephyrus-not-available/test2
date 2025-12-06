# =====================================================================
# Multi-stage Dockerfile for KTU Voting Application
# Optimized for 1500+ concurrent users with Java 21
# =====================================================================

# Build stage: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn -B clean package -DskipTests

# Runtime stage: Use optimized JRE image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root user for security
RUN groupadd --gid 1000 app || true && \
    useradd --uid 1000 --gid 1000 --create-home --shell /bin/sh app || true

# Copy the JAR file from build stage
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} /app/app.jar

# Install curl for healthchecks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Expose application port
EXPOSE 8080

# Expose actuator port for monitoring
EXPOSE 9090

# JVM options optimized for high concurrency (1500+ users)
# -Xms and -Xmx: Heap size (adjust based on available RAM)
# -XX:+UseG1GC: G1 garbage collector (best for large heaps)
# -XX:MaxGCPauseMillis: Target GC pause time
# -XX:+UseStringDeduplication: Reduce memory usage
# -XX:+OptimizeStringConcat: Optimize string operations
ENV JAVA_OPTS="-XX:+UseG1GC \
-Xms2g -Xmx4g \
-XX:MaxGCPauseMillis=200 \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/tmp/heapdump.hprof \
-Djava.security.egd=file:/dev/./urandom \
-Djava.awt.headless=true \
-Dfile.encoding=UTF-8"

# Run as non-root user
USER app

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
