# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ARG APP_INSIGHTS_AGENT_VERSION=2.6.1
ARG PLATFORM=""

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:11-distroless
LABEL maintainer="https://github.com/hmcts/ccd-definition-store-api"

COPY build/libs/case-definition-store-api.jar /opt/app/
COPY lib/AI-Agent.xml /opt/app/

EXPOSE 4451

CMD ["case-definition-store-api.jar"]
