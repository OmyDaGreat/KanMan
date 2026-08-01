ARG TARGETARCH
ARG BUILD_SHA=unknown

# Build Stage
FROM eclipse-temurin:26-jdk AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

ARG BUILD_SHA
ENV BUILD_SHA=$BUILD_SHA

COPY . .

RUN ./gradlew :site:dockerRuntime --no-daemon --no-build-cache --no-configuration-cache

# Runtime Stage
FROM eclipse-temurin:26-jre

ARG BUILD_SHA
LABEL org.opencontainers.image.revision=$BUILD_SHA

WORKDIR /app

COPY --from=builder /app/site/build/docker /app

EXPOSE 6320

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget --spider -q http://localhost:6320/api/health || exit 1

# Environment Variables
ENV PORT=6320

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -cp /app/lib/*:/app/app.jar xyz.malefic.kanman.server.KanManKt"]
