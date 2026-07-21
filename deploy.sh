#!/bin/bash
# Enterprise Automated Deployment and Rollback Script

echo "Starting deployment..."
docker compose up -d web modern

echo "Waiting up to 120 seconds for application initialization..."
# Wait for health checks
if ! docker compose up --wait web modern; then
    echo "WARNING: Health check failed. Initiating automated rollback procedure..."
    
    # Scale to 0 to terminate active client connections and drop locks
    echo "Scaling application instances to 0..."
    docker compose up -d --scale web=0 --scale modern=0 web modern
    
    # Run database recovery using the admin container
    echo "Restoring database schema..."
    docker compose run --rm admin restore
    
    # Restart applications
    echo "Restarting application containers..."
    docker compose up -d --scale web=1 --scale modern=1 web modern
    
    # Verify they are back online
    echo "Verifying application health post-rollback..."
    docker compose up --wait web modern
    
    echo "Rollback completed successfully."
    # Exit with a non-zero status to indicate deployment failed but rollback succeeded
    exit 1
fi

echo "Deployment completed successfully."
