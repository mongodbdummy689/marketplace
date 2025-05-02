# Use Maven with JDK 17 as the base image
FROM maven:3.9.5-eclipse-temurin-17

# Set the working directory
WORKDIR /app

# Copy the pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Use a smaller base image for the runtime
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=0 /app/target/marketplace-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"] 