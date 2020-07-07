# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ARG APP_INSIGHTS_AGENT_VERSION=2.6.1

FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4
LABEL maintainer="https://github.com/hmcts/ccd-definition-store-api"

COPY build/libs/case-definition-store-api.jar /opt/app/
COPY lib/AI-Agent.xml /opt/app/

EXPOSE 4451

CMD ["case-definition-store-api.jar"]
