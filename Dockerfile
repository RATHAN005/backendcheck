# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
RUN mkdir -p /app/uploads
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /uploads && chmod 777 /uploads
ENTRYPOINT ["java","-jar","/app.jar"]
