FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
# Shared build stage is cached across all four service images.
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG SERVICE
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /workspace/${SERVICE}/build/libs/${SERVICE}.jar app.jar
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]
