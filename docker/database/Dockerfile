# Change to non-root privilege
USER hmcts

FROM postgres:15

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d ccd_definition -U ${DEFINITION_STORE_DB_USERNAME}

EXPOSE 5432
