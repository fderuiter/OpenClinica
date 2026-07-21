#!/bin/bash
# Enterprise Automated Deployment and Rollback Script

WEB_IMG=$(docker compose images -q web 2>/dev/null)
MODERN_IMG=$(docker compose images -q modern 2>/dev/null)
if [ -n "$WEB_IMG" ]; then
    echo "Tagging current web image ($WEB_IMG) as stable..."
    docker tag "$WEB_IMG" app-web:stable
fi
if [ -n "$MODERN_IMG" ]; then
    echo "Tagging current modern image ($MODERN_IMG) as stable..."
    docker tag "$MODERN_IMG" app-modern:stable
fi

echo "Initiating database backup prior to deployment..."
docker compose run --rm admin backup

echo "Starting deployment..."
docker compose up -d --no-build web modern

echo "Waiting up to 50 seconds for application initialization..."
# Wait for health checks
HEALTHY_DEPLOY=false
for i in {1..10}; do
    ALL_HEALTHY=true
    
    CONTAINERS=$(docker compose ps -q web modern)
    
    if [ -z "$CONTAINERS" ]; then
        ALL_HEALTHY=false
    else
        for container_id in $CONTAINERS; do
            status=$(docker inspect --format='{{.State.Health.Status}}' "$container_id" 2>/dev/null)
            if [ "$status" != "healthy" ]; then
                ALL_HEALTHY=false
                break
            fi
        done
    fi
    
    if [ "$ALL_HEALTHY" = true ]; then
        HEALTHY_DEPLOY=true
        break
    fi
    
    sleep 5
done

if [ "$HEALTHY_DEPLOY" = false ]; then
    echo "WARNING: Health check failed. Initiating automated rollback procedure..."
    
    # Scale to 0 to terminate active client connections and drop locks
    echo "Scaling application instances to 0..."
    docker compose up -d --no-build --scale web=0 --scale modern=0 web modern
    
    # Run database recovery using the admin container
    echo "Restoring database schema..."
    docker compose run --rm admin restore
    
    # Restart applications
    echo "Restarting application containers..."
    IMAGE_TAG=stable docker compose up -d --no-build --scale web=1 --scale modern=1 web modern
    
    # Verify they are back online
    echo "Verifying application health post-rollback..."
    if ! IMAGE_TAG=stable docker compose up --no-build --wait web modern; then
        echo "ERROR: Rollback health check failed. Manual intervention required."
        exit 1
    fi
    
    echo "Rollback completed successfully."
    exit 1
fi

echo "Deployment completed successfully."
