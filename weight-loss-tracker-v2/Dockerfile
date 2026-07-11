FROM node:24-alpine AS web-build
WORKDIR /build/web
COPY web/package.json web/package-lock.json ./
RUN npm ci
COPY web/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build
WORKDIR /build/backend
COPY backend/ ./
COPY --from=web-build /build/web/dist/ src/main/resources/static/
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/data \
    && chown -R app:app /app
COPY --from=backend-build --chown=app:app /build/backend/target/weight-loss-tracker-backend-*.jar /app/app.jar
USER app
EXPOSE 8080
VOLUME ["/app/data"]
HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=5 \
    CMD wget --spider -q http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
