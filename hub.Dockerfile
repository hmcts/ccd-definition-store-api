FROM gradle:4.10-jdk8 as builder
LABEL maintainer="https://github.com/hmcts/ccd-definition-store-api"

COPY . /home/gradle/src
USER root
RUN chown -R gradle:gradle /home/gradle/src
USER gradle

WORKDIR /home/gradle/src
RUN gradle assemble

FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.4

ENV APP case-definition-store-api.jar
ENV APPLICATION_TOTAL_MEMORY 950M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 90

ENV JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom"

COPY --from=builder /home/gradle/src/build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4451/status/health

EXPOSE 4451
