FROM openjdk:17-jdk-slim

WORKDIR /app

# Build edilen JAR dosyasını target klasöründen al ve container içine kopyala
COPY target/*.jar /app/microservice-b.jar

# Sentry properties dosyasını kopyala
COPY sentry.properties /app/sentry.properties

# Sentry OpenTelemetry Agent dosyasını bulunduğu dizinden al ve container içine kopyala
COPY sentry-opentelemetry-agent-8.2.0.jar /app/sentry-agent.jar

# SENTRY_API_KEY'i ortam değişkeninden al
ARG SENTRY_API_KEY

# SENTRY_PROPERTIES_FILE ortam değişkenini tanımla
ENV SENTRY_PROPERTIES_FILE=/app/sentry.properties

# Uygulamayı başlat
ENTRYPOINT ["java", \
"-Dotel.service.name=microservice-b", \
"-Dotel.exporter.otlp.endpoint=http://otel-collector:4318", \
"-Dotel.exporter.otlp.headers=sentry-trace=true,sentry-key=d819139e4e0d8db33b80a74b19021eb7", \
"-Dotel.traces.exporter=otlp", \
"-Dotel.metrics.exporter=otlp", \
"-Dotel.logs.exporter=otlp", \
"-Dotel.resource.attributes=environment=test", \
"-javaagent:/app/sentry-agent.jar", \
"-jar", "/app/microservice-b.jar"]
