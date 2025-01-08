FROM eclipse-temurin:23.0.1_11-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the jar file into the container
COPY target/wangbot-1.0-SNAPSHOT.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]