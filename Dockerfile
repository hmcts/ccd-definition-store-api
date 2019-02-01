FROM hmcts/cnp-java-base:openjdk-8u181-jre-alpine3.8-1.0
LABEL maintainer="https://github.com/hmcts/ccd-definition-store-api"

ENV APP case-definition-store-api.jar
ENV APPLICATION_TOTAL_MEMORY 950M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 90

ENV JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom"

COPY build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4451/status/health

EXPOSE 4451
