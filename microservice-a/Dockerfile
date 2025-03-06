FROM openjdk:17-jdk-slim

WORKDIR /app

MAINTAINER hasankadirdemircan

# Copy Built jar
COPY target/*.jar /app/microservice-a.jar

# Copy Sentry Properties file
COPY sentry.properties /app/sentry.properties

# Sentry OpenTelemetry Agent file path
COPY sentry-opentelemetry-agent-8.2.0.jar /app/sentry-agent.jar

# SENTRY_PROPERTIES_FILE
ENV SENTRY_PROPERTIES_FILE=/app/sentry.properties

# Run
ENTRYPOINT ["java", \
"-Dotel.service.name=microservice-a", \
"-Dotel.exporter.otlp.endpoint=http://otel-collector:4318", \
"-Dotel.exporter.otlp.headers=sentry-trace=true,sentry-key=d819139e4e0d8db33b80a74b19021eb7", \
"-Dotel.traces.exporter=otlp", \
"-Dotel.metrics.exporter=otlp", \
"-Dotel.logs.exporter=otlp", \
"-Dotel.resource.attributes=environment=test", \
"-javaagent:/app/sentry-agent.jar", \
"-jar", "/app/microservice-a.jar"]

