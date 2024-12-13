# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ARG APP_INSIGHTS_AGENT_VERSION=3.5.4
ARG PLATFORM=""

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:21-distroless

# Change to non-root privilege
USER hmcts

LABEL maintainer="https://github.com/hmcts/ccd-definition-store-api"

COPY build/libs/case-definition-store-api.jar /opt/app/
COPY lib/applicationinsights.json /opt/app

EXPOSE 4451

CMD ["case-definition-store-api.jar"]
