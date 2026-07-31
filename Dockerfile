ARG TARGETARCH
ARG BUILD_SHA=unknown

# Build Stage
FROM eclipse-temurin:26-jdk AS builder

WORKDIR /app

# Copy gradle executable and wrapper
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Copy build configuration for dependency caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY site/build.gradle.kts ./site/

# Prefetch dependencies
RUN ./gradlew :site:dependencies --no-daemon || true

# Accept BUILD_SHA to bust cache for source code changes
ARG BUILD_SHA
ENV BUILD_SHA=$BUILD_SHA

# Copy the rest of the source code
COPY . .

RUN ./gradlew :site:dockerRuntime --no-daemon

# Runtime Stage
FROM eclipse-temurin:26-jre

LABEL org.opencontainers.image.revision=$BUILD_SHA

WORKDIR /app

COPY --from=builder /app/site/build/docker /app

EXPOSE 6320

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget --spider -q http://localhost:6320/api/health || exit 1

# Environment Variables
ENV PORT=6320

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -cp /app/lib/*:/app/app.jar xyz.malefic.kanman.server.KanManKt"]
