# ── Build ────────────────────────────────────────────────────────────────────
# Official Maven image, so the build doesn't depend on the Maven wrapper
# downloading Maven with curl/wget inside the container.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B package -DskipTests

# ── Run ──────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Hosting platforms inject PORT; application.properties reads it
ENV PORT=8080
# Keep the JVM within small container memory limits (e.g. Render free: 512 MB)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
