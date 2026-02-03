#!/bin/bash

echo "🚀 Laundry Scheduler Setup"
echo "=========================="
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "📝 Creating .env file from .env.example..."
    cp .env.example .env
    echo "✅ .env file created. Please edit it with your settings!"
    echo ""
fi

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "🐳 Building and starting Docker containers..."
if docker compose version &> /dev/null; then
    docker compose up -d --build
else
    docker-compose up -d --build
fi

echo ""
echo "⏳ Waiting for services to start..."
sleep 10

echo ""
echo "✅ Setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Access the application at http://localhost"
echo "2. Sign up for your first account"
echo "3. Make yourself an admin by running:"
echo "   docker exec -it laundry-postgres psql -U laundry_user -d laundry_db"
echo "   UPDATE users SET is_admin = true WHERE username = 'your_username';"
echo ""
echo "📧 Don't forget to configure email settings in .env for notifications!"
echo ""

