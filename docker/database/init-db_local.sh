#!/usr/bin/env bash

set -e


# Create role and database
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME='ccd' --set PASSWORD='password' <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';
  CREATE DATABASE ccd_definition
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
  ALTER SCHEMA public OWNER TO :USERNAME;
EOSQL
