#!/bin/bash
set -e

# --- Step 1: Detect .NET SDK ---
if ! command -v dotnet &> /dev/null
then
    echo "❌ .NET SDK not found. Please install it from https://dotnet.microsoft.com/download"
    exit 1
fi

echo "✅ .NET SDK version:"
dotnet --version

# --- Step 2: Move to project directory ---
cd "$(dirname "$0")/../asp-user"
echo "📂 Current directory: $(pwd)"

# --- Step 3: Restore dependencies ---
echo "🔄 Restoring dependencies..."
dotnet restore

# --- Step 4: Build the service ---
echo "🏗️ Building ASP.NET Core User Service..."
dotnet build --no-restore

# --- Step 5: Run ---
echo "🚀 Starting ASP.NET Core User Service..."
dotnet run --no-build
