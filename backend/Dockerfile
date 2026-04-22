FROM eclipse-temurin:17-jdk AS build

ARG SERVICE_NAME

WORKDIR /workspace

COPY gradle gradle
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY bp-ms-account/build.gradle.kts bp-ms-account/build.gradle.kts
COPY bp-ms-customer/build.gradle.kts bp-ms-customer/build.gradle.kts
COPY bp-ms-gateway/build.gradle.kts bp-ms-gateway/build.gradle.kts

RUN chmod +x gradlew

# Download dependencies first (layer caching)
RUN ./gradlew :${SERVICE_NAME}:dependencies --no-daemon || true

COPY bp-ms-account/src bp-ms-account/src
COPY bp-ms-customer/src bp-ms-customer/src
COPY bp-ms-gateway/src bp-ms-gateway/src

RUN ./gradlew :${SERVICE_NAME}:bootJar -x test --no-daemon

# The root build.gradle.kts redirects buildDirectory to java.io.tmpdir
RUN cp /tmp/gradle-build/${SERVICE_NAME}/libs/${SERVICE_NAME}-1.0.0.jar /workspace/app.jar

FROM eclipse-temurin:17-jre

RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser

WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

COPY --from=build /workspace/app.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
