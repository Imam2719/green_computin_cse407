# Use Eclipse Temurin JDK 17 with Alpine Linux as the base image
FROM eclipse-temurin:17-jdk-alpine

# Install necessary packages
RUN apk add --no-cache bash

# Set working directory in container
WORKDIR /app

# Copy the Maven wrapper files first
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Give execution permission to mvnw
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the project
COPY src ./src/

# Build the project
RUN ./mvnw clean package -DskipTests

# Set Java options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Get the PORT environment variable
ENV PORT=8080

# Run the application with the PORT environment variable
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar -Dserver.port=${PORT} target/Green_Computing_CSE407-0.0.1-SNAPSHOT.jar"]
