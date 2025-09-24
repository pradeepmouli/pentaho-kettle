#!/bin/bash

# Simple script to start the React development server for testing

echo "Starting Spoon Web UI Development Server..."

cd src/main/webapp

if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

echo "Starting React development server on port 3000..."
echo "The web UI will be available at: http://localhost:3000"
echo "Press Ctrl+C to stop the server"

npm start