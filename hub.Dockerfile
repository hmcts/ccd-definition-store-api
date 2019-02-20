FROM gradle:4.10-jdk8 as builder
LABEL maintainer="https://github.com/hmcts/ccd-definition-store-api"

COPY . /home/gradle/src
USER root
RUN chown -R gradle:gradle /home/gradle/src
USER gradle

WORKDIR /home/gradle/src
RUN gradle assemble

FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-1.0

ENV JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom"

COPY --from=builder /home/gradle/src/build/libs/case-definition-store-api.jar /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4451/status/health

EXPOSE 4451

CMD ["case-definition-store-api.jar"]
