# Use Eclipse Temurin JDK 17 with Alpine Linux as the base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory in container
WORKDIR /app

# Copy the entire project first
COPY . .

# Build the project
RUN ./mvnw clean package -DskipTests

# Set Java options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar target/Green_Computing_CSE407-0.0.1-SNAPSHOT.jar"]
