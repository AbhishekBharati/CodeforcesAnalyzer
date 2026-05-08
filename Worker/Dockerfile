# Stage 1: Build
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
# Note: OPENAI_API_KEY must be passed as an environment variable at runtime
ENTRYPOINT ["java", "-jar", "app.jar"]
