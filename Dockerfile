# syntax=docker/dockerfile:1.7
#
# Multi-stage build:
# - build: uses Gradle wrapper to produce a runnable fat jar (fatJar task)
# - runtime: runs the produced *-all.jar on a JRE image
#
# Build:
#   docker build -t untitled1 .
# Run:
#   docker run --rm -p 8080:8080 untitled1
#
ARG JDK_VERSION=25

FROM eclipse-temurin:${JDK_VERSION}-jdk-jammy AS build
WORKDIR /app

# Copy only build configuration first to maximize Docker layer caching.
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

RUN chmod +x gradlew

# Source last (changes frequently).
COPY src src

RUN ./gradlew --no-daemon clean fatJar

FROM eclipse-temurin:${JDK_VERSION}-jre-jammy AS runtime
WORKDIR /app

# Create an unprivileged user to run the app.
RUN useradd --system --uid 10001 --create-home appuser
USER appuser

COPY --from=build /app/build/libs/*-all.jar /app/app.jar

EXPOSE 8080

# Optional: set extra JVM flags at runtime, e.g.:
#   docker run -e JAVA_OPTS="-Xms128m -Xmx256m" ...
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

