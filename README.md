# ccd-case-definition-store-api
[![API Docs](https://img.shields.io/badge/API%20Docs-site-e140ad.svg)](https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/ccd-definition-store-api.json)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://api.travis-ci.org/hmcts/ccd-definition-store-api.svg?branch=master)](https://travis-ci.org/hmcts/ccd-definition-store-api)
[![Docker Build Status](https://img.shields.io/docker/build/hmcts/ccd-definition-store-api.svg)](https://hub.docker.com/r/hmcts/ccd-definition-store-api)
[![codecov](https://codecov.io/gh/hmcts/ccd-definition-store-api/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/ccd-definition-store-api)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d3b02d95faf6419ca6fbb15b2e712b8b)](https://www.codacy.com/app/adr1ancho/ccd-definition-store-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hmcts/ccd-definition-store-api&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/d3b02d95faf6419ca6fbb15b2e712b8b)](https://www.codacy.com/app/adr1ancho/ccd-definition-store-api?utm_source=github.com&utm_medium=referral&utm_content=hmcts/ccd-definition-store-api&utm_campaign=Badge_Coverage)
[![Known Vulnerabilities](https://snyk.io/test/github/hmcts/ccd-definition-store-api/badge.svg)](https://snyk.io/test/github/hmcts/ccd-definition-store-api)
[![HitCount](http://hits.dwyl.io/hmcts/ccd-definition-store-api.svg)](#ccd-definition-store-api)
  
Validation and persistence of definitions for field types, jurisdictions, case types and associated display elements.

## Overview

Definitions are imported as an Excel spreadsheet which are parsed, persisted and then exposed as JSON through a REST API.

Spring Boot and Spring Data are used to persist the data in a PostgreSQL database. The database schema is created and maintained by Flyway change sets applied during application startup.

Moreover, if the feature is enabled, the ElasticSearch cluster is initialised when a definition file is imported. For each case type, an index, an alias, 
and a mapping is created on ElasticSearch. If `failOnImport` is true, any ES initialisation error will prevent the import to succeed. If false, ES errors are
simply ignored

## Getting started

### Prerequisites

- [Open JDK 21](https://openjdk.java.net/)
- [Docker](https://www.docker.com)

#### Environment variables

The following environment variables are required:

| Name | Default                                                                     | Description |
|------|-----------------------------------------------------------------------------|-------------|
| DEFINITION_STORE_DB_USERNAME | -                                                                           | Username for database |
| DEFINITION_STORE_DB_PASSWORD | -                                                                           | Password for database |
| DEFINITION_STORE_DB_USE_SSL | -                                                                           | set to `true` if SSL is to be enabled. `false` recommended for local environments. |
| DEFINITION_STORE_IDAM_KEY | -                                                                           | Definition store's IDAM S2S micro-service secret key. This must match the IDAM instance it's being run against. |
| DEFINITION_STORE_S2S_AUTHORISED_SERVICES | ccd_data,ccd_gw,ccd_admin,jui_webapp,pui_webapp,aac_manage_case_assignment,xui_webapp | Authorised micro-service names for S2S calls |
| IDAM_USER_URL | -                                                                           | Base URL for IdAM's User API service (idam-app). `http://localhost:4501` for the dockerised local instance or tunneled `dev` instance. |
| IDAM_S2S_URL | -                                                                           | Base URL for IdAM's S2S API service (service-auth-provider). `http://localhost:4502` for the dockerised local instance or tunneled `dev` instance. |
| USER_PROFILE_HOST | -                                                                           | Base URL for the User Profile service. `http://localhost:4453` for the dockerised local instance. |
| AZURE_APPLICATIONINSIGHTS_INSTRUMENTATIONKEY | -                                                                           | secrets for Microsoft Insights logging, can be a dummy string in local |


The following environment variables are optional:

| Name | Default            | Description |
|------|--------------------|-------------|
| ENABLE_CASE_GROUP_ACCESS                     | true/false         | Enable case group access filtering.                                                                                                                                                 |
| GROUP_ACCESS_ENABLED                         | true/false/not set | Enable group access Testing (Funtional Tests).                                                                                                                                       |

### Building

The project uses [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). 

This project uses [TestContainers](https://www.testcontainers.org/usage/database_containers.html#jdbc-url) for the database testing support.
Docker must be installed on the machine you are running tests. 

To build project please execute the following command:

```bash
./gradlew clean build
```

### Running

If you want your code to become available to other Docker projects (e.g. for local environment testing), you need to build the image:

```bash
docker-compose build
```

The above will build both the application and database images.  
If you want to build only one of them just specify the name assigned in docker compose file, e.g.:

```bash
docker-compose build ccd-definition-store-api
```

When the project has been packaged in `target/` directory, 
you can run it by executing following command:

```bash
docker-compose up
```

As a result the following containers will get created and started:

 - Database exposing port `5451`
 - API exposing ports `4451`

#### Handling database

Database will get initiated when you run `docker-compose up` for the first time by execute all scripts from `database` directory.

You don't need to migrate database manually since migrations are executed every time `docker-compose up` is executed.

You can connect to the database at `http://localhost:5451` with the username and password set in the environment variables.

## Modules

The application is structured as a multi-module project. The modules are:

### repository

Data access layer.

### domain

Domain logic.

### rest-api

Secured RESTful API giving access to part of the domain logic.

### excel-importer

Secured endpoint and specific logic for importing case definition as an Excel spreadsheet.

### application

Spring application entry point and configuration.

### Functional Tests
The functional tests are located in `aat` folder. The tests are written using 
befta-fw library. To find out more about BEFTA Framework, see the repository and its README [here](https://github.com/hmcts/befta-fw).

#### To Run the Functional Tests (FT)

#####  All Functional Tests

Will run all the FT's:

```bash
./gradlew functional
```

#####  Some Functional Tests
Will run both F-105 and F-110:

```bash
./gradlew functional -P tags="@F-105 or @F-110"
```

Will run only S-110.1:

```bash
./gradlew functional -P tags="@S-110.1"
```


### Case type snapshot updates

Snapshots are cached copies of case type responses. The application continues to use the existing
`case_type_snapshot` table after this update. You can import and read definitions as usual;
there is no need to move data or re-import definitions.

#### What happens when you deploy?

With the default configuration, Flyway automatically runs migration `V20260930_01` during startup,
before the application serves requests. No manual migration step or new Docker/Flux environment
variables are needed.

The migration adds two columns and a database function and trigger to the existing table.
It keeps existing definitions and cached responses. Older cached responses are marked for rebuilding
the next time they are requested.

During deployment:

- The database account running Flyway needs permission to add the columns, function and trigger.
- Updating the table requires an exclusive lock. Long-running transactions can delay startup,
  and requests using the snapshot table may wait. Prefer a quieter deployment period and monitor locks.
- Rebuilding older snapshots temporarily adds database work. Monitor connection usage, response times
  and snapshot-write errors until the cache has warmed up.
- If this migration has already been applied in an environment, make further schema changes in a
  new migration file rather than editing the applied file.

#### Running old and new application versions together

The database trigger marks snapshots written by older applications for rebuilding, so newer
applications do not mistake them for responses in the new format.

Older applications do not check the snapshot format. If a release introduces responses they cannot
read correctly, set `CASE_TYPE_SNAPSHOT_ENABLED=false` on those older instances before running the
versions together. They will read definitions directly instead.

#### Rolling back the application

Keep the added columns, function and trigger when rolling back. If the older application cannot read
the newer cached responses correctly, disable its snapshot cache with `CASE_TYPE_SNAPSHOT_ENABLED=false`.
Follow the existing Flyway version-validation process; do not remove columns while newer instances
are still running.

#### Notes for developers

- Increase `SnapshotFormat.REVISION` when mapping or response-model changes make existing snapshots
  obsolete, even if no definition has been re-imported. Revision 0 identifies older snapshots.
- Writers that set a format revision must also advance `format_write_count`. Otherwise, the trigger
  resets the revision to 0 so the response can be rebuilt safely.
- The new upsert prevents overwriting a higher definition version or format revision. It can repair
  an unreadable snapshot without changing the definition version. A cache miss does not delete rows.
- Snapshot writes happen after the read transaction releases its database connection. If the caller
  still has an active transaction, the optional cache write is skipped to avoid needing a second connection.

## LICENSE

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
