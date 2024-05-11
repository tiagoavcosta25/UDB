#!/bin/bash
# set -x  # Debug mode

HOST=${1:-"localhost"}
ENDPOINT="http://${HOST}:8091/pools"

MAX_ATTEMPTS=${2:-5}
ATTEMPT=0

# Wait for Couchbase Server to be up and running
echo "Checking Couchbase Server $HOST status..."
until curl -s "$ENDPOINT" > /dev/null; do
    ((ATTEMPT++))  # Increment the attempt counter

    # Check if the maximum attempts have been reached
    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo "Failed to connect to Couchbase Server $HOST after $MAX_ATTEMPTS attempts."
        exit 1  # Exit with failure
    fi

    sleep 5
done

echo "Couchbase Server $HOST is up and running"
exit 0  # Exit with success
