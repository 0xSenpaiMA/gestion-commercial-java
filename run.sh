#!/bin/bash

echo "Starting Gestion Commerciale Application..."
echo

# Set Java options
JAVA_OPTS="-Xmx1024m -Dfile.encoding=UTF-8"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Java is not installed or not in PATH. Please install Java 17 or higher."
    exit 1
fi

# Try to compile with Maven if available
if [ ! -d "target/classes" ]; then
    echo "Compiling project..."
    if [ -f "mvnw" ]; then
        ./mvnw compile
    else
        echo "Maven not found. Please compile the project manually in your IDE."
        echo "Press any key to try running anyway..."
        read -n 1
    fi
fi

echo
echo "Starting application..."
java $JAVA_OPTS -cp "target/classes:target/dependency/*" com.gestioncommerciale.GestionCommercialeApp

if [ $? -ne 0 ]; then
    echo
    echo "Application exited with error code $?"
    echo "Please check the logs for more information."
    read -p "Press any key to continue..."
fi
