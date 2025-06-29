# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create a slim, final image with the compiled application
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/shrinker-kit-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]