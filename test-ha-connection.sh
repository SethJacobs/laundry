#!/bin/bash
echo "Testing Home Assistant Connection..."
echo ""

# Check if backend is running
if ! docker compose ps backend | grep -q "Up"; then
    echo "❌ Backend container is not running"
    exit 1
fi

echo "✅ Backend container is running"
echo ""

# Check environment variables
echo "Environment Variables in Backend:"
docker compose exec -T backend env | grep HA_ | head -5
echo ""

# Test API endpoint (requires auth token - this is just to see if endpoint exists)
echo "Testing /api/machines/washer endpoint..."
echo "Note: This requires authentication, so you'll need to be logged in via the UI"
echo ""

# Check backend logs for HA connection errors
echo "Recent HA-related errors in logs:"
docker compose logs backend 2>&1 | grep -iE "Failed to fetch HA|homeassistant|HA entity" | tail -5 || echo "No HA errors found in recent logs"

