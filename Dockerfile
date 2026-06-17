ARG TARGETARCH

# Build Stage
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties ./

RUN chmod +x ./gradlew

COPY . .

RUN ./gradlew :site:dockerRuntime

# Runtime Stage
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/site/build/docker /app

EXPOSE 6320

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget --spider -q http://localhost:6320/api/health || exit 1

# Environment Variables
ENV PORT=6320

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -cp /app/lib/*:/app/app.jar xyz.malefic.kanman.KanManKt"]
