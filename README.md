# spring--opentelemetry-sentry

Spring Boot - OpenTelemetry - Sentry Entegrasyonu
sentry-diagram.png

İmplementasyon Adımları
Spring Boot Dependencies
Spring Boot application.yml
Sentry OpenTelemetry Agent
OpenTelemetry(config, docker-compose)
Sentry Nedir?
Sentry, yazılım geliştiricilerinin uygulamalarındaki hataları (error) ve performans sorunlarını gerçek zamanlı olarak izlemelerine ve çözmelerine yardımcı olan bir hata izleme (error tracking) ve performans izleme aracıdır.

Çeşitli programlama dilleri ve framework’lerle entegre çalışarak hataların nerede, nasıl ve neden meydana geldiğini detaylı bir şekilde raporlar.

Sentry’nin "Disturbed" özelliği, kritik hatalar veya önemli olaylar meydana geldiğinde, geliştiricilere anında bildirim göndermeyi sağlar.

Böylece ekipler, sorunlardan anında haberdar olup hızlı aksiyon alabilir. Bu özellik, özellikle üretim ortamındaki uygulamalarda kesintileri en aza indirmek ve kullanıcı deneyimini korumak için oldukça önemlidir.

Sentry api call time, error count gibi metriklerle chart’lar oluşturabiliyor.

Eğer Spring Boot Actuator metriklerini de entegre etmek istiyorsak direkt olarak Spring Boot actuator metrik entegrasyonu bulunmamaktadır. Fakat Sentry Grafana entegrasyonu vardır.( https://sentry.io/integrations/grafana/)

Charts
sentry-chart-01.png sentry-chart-02.png

Loglar izlenebilir
sentry-log-01.png
Farklı microservisler arasında aynı trackId kullanarak hangi istekte hangi serviste hata ile karşılaşıldığını görebilme özelliğine sahiptir.
sentry-trace-01.png

İmplementasyon
Spring Boot Dependencies
Eğer actuator da kullanacaksak pom.xml'e eklenmelidir. (Sentry + Grafana Integration)
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
	<version>3.4.3</version>
</dependency>
Not: sentry-opentelemetry-agent kullanarak OpenTelemetry'i besleyeceğimiz için sentry-spring-boot-starter-jakarta'ya gerek yok.

Spring Boot application.yml Config
spring:
  application:
    name: c

server:
  port: 9096

management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,prometheus,traces"

  endpoint:
    health:
      show-details: "always"

  metrics:
    export:
      prometheus:
        enabled: true

Not: Microservislerden trace-id üretmek ve distributed olarak logları sentry tarafında birleştirmek için sentry-opentelemetry-agent-8.2.0.jar kullanmamız gerekiyor. Burada sentry.properties tanımlayacağız ve dsn bilgisini ekleyeceğiz.

dsn: {sentry-dsn}
traces-sample-rate: 1.0 
Örnek bir microservice run
SENTRY_PROPERTIES_FILE=sentry.properties \
java \
  -Dotel.service.name=demo-microservice \
  -Dotel.exporter.otlp.endpoint=http://localhost:4318 \
  -Dotel.traces.exporter=otlp \
  -Dotel.metrics.exporter=otlp \
  -Dotel.logs.exporter=otlp \
  -Dotel.exporter.otlp.headers="sentry-trace=true,sentry-key=895cf29169ca8cdfa8eeaa546a713472" \
  -javaagent:sentry-opentelemetry-agent-8.2.0.jar \
  -jar ./target/demo-0.0.1-SNAPSHOT.jar
{sentry-api-key}
Not: {sentry-api-key} oluşturmak için Sentry UI -> Settings -> Api Keys seçeneğinden oluşturabilirsiniz.

sentry-opentelemetry-agent
Spring Boot logları, metricleri otomatik OpenTelemetry’e akması için;
https://mvnrepository.com/artifact/io.sentry/sentry-opentelemetry-agent/8.2.0 jar’ına ihtiyacımız vardır.
mvnrepository linkinden indirilebilir.
OpenTelemetry
OpenTelemetry (OTel), uygulamalarınızın gözlemlenebilirliğini (observability) artırmak için kullanılan açık kaynaklı bir framework’tür.

Tracing (izleme), Metrics (ölçümler) ve Logs (loglar) toplamak için kullanılır.

OpenTelemetry docker ile ayağa kaldırabiliriz;

Spring Boot mikroservislerinden gelen loglar, metrikler hangi şekilde ve hangi sentry’e akacağı konfigürasyonları otel-collector-config.yaml içerisinde yapacağız.

otel-collector-config.yml şu şekilde, bunu docker-compose.yml içerisinde kullanacağız.

receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4318
      grpc:
        endpoint: 0.0.0.0:4317

processors:
  batch:
    timeout: 1s
    send_batch_size: 1024
  memory_limiter:
    check_interval: 1s
    limit_mib: 1000

exporters:
  debug:
    verbosity: detailed
  sentry:
    dsn: {sentry-dsn}

service:
  telemetry:
    logs:
      level: "debug"
  pipelines:
    traces:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [debug, sentry]
    metrics:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [debug]
    logs:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [debug]
docker-compose.yml şu şekilde;
version: '3.8'

services:
  # OpenTelemetry Collector
  otel-collector:
    image: ghcr.io/open-telemetry/opentelemetry-collector-releases/opentelemetry-collector-contrib:latest
    command: ["--config=/etc/otel-collector-config.yaml"]
    volumes:
      - ./otel-collector-config.yaml:/etc/otel-collector-config.yaml
    ports:
      - "4318:4318"   # OTLP HTTP receiver
      - "4317:4317"   # OTLP gRPC receiver
      - "8888:8888"   # Prometheus metrics exposed by the collector
      - "8889:8889"   # Prometheus exporter metrics
Not: microservisler için Dockerfile tanımlayıp, docker-compose içerisine ekleyebiliriz.

Örnek Dockerfile
FROM maven:3.8.3-openjdk-17 AS MAVEN_BUILD

MAINTAINER t

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build/

RUN mvn package

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/*.jar /app/demo.jar
COPY sentry-opentelemetry-agent-8.2.0.jar /app/sentry-agent.jar

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "demo.jar"]

ENTRYPOINT ["java", \
"-Dotel.service.name=demo", \
"-Dotel.exporter.otlp.endpoint=http://otel-collector:4318", \
"-Dotel.traces.exporter=otlp", \
"-Dotel.metrics.exporter=otlp", \
"-Dotel.logs.exporter=otlp", \
"-Dotel.exporter.otlp.headers=sentry-trace=true,sentry-key={sentry-api-key}", \
"-javaagent:sentry-agent.jar", \
"-jar", "demo.jar"]
Not: Build aşamasını çıkartabiliriz. Sadece örnek için hazırlanmıştır.