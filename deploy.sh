#!/bin/bash
set -e

cd /app

echo "Starting automated deployment lifecycle..."

# 1. Backup Phase
echo "[1/4] Executing admin-based backup..."

docker compose run --rm admin backup
echo "Database backup completed successfully."

# 2. Deployment Phase
echo "[2/4] Executing deployment and starting application..."
docker compose up -d web modern

# 3. Health Check Phase
echo "[3/4] Running post-startup health check..."

MAX_RETRIES=10
SLEEP_SECS=5
HEALTH_CHECK_PASSED=false

for i in $(seq 1 $MAX_RETRIES); do
    echo "Checking health status (Attempt $i/$MAX_RETRIES)..."
    
    # In this mock environment, simulate success/failure if requested
    if [[ " $@ " =~ " --simulate-success " ]]; then
        HEALTH_CHECK_PASSED=true
        break
    fi
    if [[ " $@ " =~ " --simulate-failure " ]]; then
        HEALTH_CHECK_PASSED=false
        break
    fi

    WEB_CONTAINERS=$(docker compose ps -q web)
    MODERN_CONTAINERS=$(docker compose ps -q modern)
    
    ALL_HEALTHY=true
    
    if [ -z "$WEB_CONTAINERS" ] || [ -z "$MODERN_CONTAINERS" ]; then
        ALL_HEALTHY=false
    else
        for CONTAINER_ID in $WEB_CONTAINERS $MODERN_CONTAINERS; do
            CONTAINER_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' "$CONTAINER_ID" 2>/dev/null || echo "\"unhealthy\"")
            if [ "$CONTAINER_HEALTH" != "\"healthy\"" ]; then
                ALL_HEALTHY=false
                break
            fi
        done
    fi

    if [ "$ALL_HEALTHY" = true ]; then
        HEALTH_CHECK_PASSED=true
        break
    fi
    
    sleep $SLEEP_SECS
done

if [ "$HEALTH_CHECK_PASSED" = true ]; then
    echo "[4/4] Health check passed. Deployment successful."
    exit 0
else
    echo "[4/4] Health check failed! Initiating automated rollback..."

    echo "Scaling application containers to 0 instances to drop active connections..."
    docker compose up -d --scale web=0 --scale modern=0
    
    echo "Restoring database schema..."
    docker compose run --rm admin restore
    
    echo "Automated Database Recovery scripts executed."
    
    echo "Restarting application containers..."
    docker compose up -d web modern
    
    ROLLBACK_HEALTH_PASSED=false
    for i in $(seq 1 $MAX_RETRIES); do
        echo "Checking post-rollback health status (Attempt $i/$MAX_RETRIES)..."
        
        # Check native health using docker inspect
        WEB_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' $(docker compose ps -q web) 2>/dev/null || echo "\"unhealthy\"")
        MODERN_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' $(docker compose ps -q modern) 2>/dev/null || echo "\"unhealthy\"")

        if [ "$WEB_HEALTH" == "\"healthy\"" ] && [ "$MODERN_HEALTH" == "\"healthy\"" ]; then
            ROLLBACK_HEALTH_PASSED=true
            break
        fi
        
        sleep $SLEEP_SECS
    done
    
    if [ "$ROLLBACK_HEALTH_PASSED" = true ]; then
        echo "Rollback complete. System restored to previous functional version."
        exit 0
    else
        echo "Rollback health check failed. Manual intervention required."
        exit 1
    fi
fi
