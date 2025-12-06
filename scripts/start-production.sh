#!/bin/bash
# =====================================================================
# Production Startup Script for KTU Voting Application
# Optimized for 1500+ concurrent users
# =====================================================================

set -e

# Configuration
APP_NAME="KTUVotingapp"
JAR_FILE="target/KTUVotingapp-*.jar"
PROFILE="prod"
LOG_DIR="./logs"
PID_FILE="./app.pid"

# JVM Options optimized for high concurrency
# Adjust heap size based on available RAM:
# - 4GB RAM: -Xms1g -Xmx2g
# - 8GB RAM: -Xms2g -Xmx4g
# - 16GB+ RAM: -Xms4g -Xmx8g
HEAP_MIN="2g"
HEAP_MAX="4g"

# Create logs directory if it doesn't exist
mkdir -p "$LOG_DIR"

# Check if JAR file exists
if ! ls $JAR_FILE 1> /dev/null 2>&1; then
    echo "ERROR: JAR file not found. Please build the project first:"
    echo "  mvn clean package"
    exit 1
fi

# Get the actual JAR file path
JAR_PATH=$(ls $JAR_FILE | head -n 1)

echo "Starting $APP_NAME..."
echo "JAR: $JAR_PATH"
echo "Profile: $PROFILE"
echo "Heap: $HEAP_MIN - $HEAP_MAX"

# JVM Options for high performance
JAVA_OPTS="-server \
-XX:+UseG1GC \
-Xms${HEAP_MIN} \
-Xmx${HEAP_MAX} \
-XX:MaxGCPauseMillis=200 \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=${LOG_DIR}/heapdump.hprof \
-Djava.security.egd=file:/dev/./urandom \
-Djava.awt.headless=true \
-Dfile.encoding=UTF-8 \
-Dspring.profiles.active=${PROFILE}"

# Start the application
nohup java $JAVA_OPTS -jar "$JAR_PATH" > "${LOG_DIR}/application.log" 2>&1 &

# Save PID
echo $! > "$PID_FILE"

echo "Application started with PID: $(cat $PID_FILE)"
echo "Logs: ${LOG_DIR}/application.log"
echo ""
echo "To check status:"
echo "  tail -f ${LOG_DIR}/application.log"
echo ""
echo "To stop:"
echo "  ./scripts/stop-production.sh"

