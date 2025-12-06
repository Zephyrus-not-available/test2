#!/bin/bash
# =====================================================================
# Production Stop Script for KTU Voting Application
# =====================================================================

PID_FILE="./app.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "ERROR: PID file not found. Application may not be running."
    exit 1
fi

PID=$(cat "$PID_FILE")

if ! ps -p "$PID" > /dev/null 2>&1; then
    echo "Application is not running (PID: $PID)"
    rm -f "$PID_FILE"
    exit 0
fi

echo "Stopping application (PID: $PID)..."

# Send SIGTERM for graceful shutdown
kill -TERM "$PID"

# Wait up to 30 seconds for graceful shutdown
for i in {1..30}; do
    if ! ps -p "$PID" > /dev/null 2>&1; then
        echo "Application stopped successfully."
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

# Force kill if still running
if ps -p "$PID" > /dev/null 2>&1; then
    echo "Force killing application..."
    kill -9 "$PID"
    rm -f "$PID_FILE"
    echo "Application force stopped."
fi

