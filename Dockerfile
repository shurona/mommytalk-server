FROM gradle:8.10-jdk21 AS builder

WORKDIR /app
COPY . /app

# Build the application using Gradle
RUN gradle clean build --no-daemon -x test

FROM openjdk:21-jdk-slim

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]