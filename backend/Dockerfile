FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Build with: ./gradlew shadowJar
COPY build/libs/*-all.jar app.jar
# Copied separately so the JVM can reference it via -Dlogback.configurationFile
COPY src/main/resources/logback-prod.xml logback-prod.xml
EXPOSE 8080
CMD ["java", "-Dlogback.configurationFile=/app/logback-prod.xml", "-jar", "app.jar"]
