FROM openjdk:8-jre

COPY application/build/libs/case-definition-store-api-*.jar /app.jar

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4451/status/health

EXPOSE 4451

CMD java ${JAVA_OPTS} -Dspring.config.location=/application.properties -Djava.security.egd=file:/dev/./urandom -jar /app.jar
