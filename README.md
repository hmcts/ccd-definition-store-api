# ccd-case-definition-store-api
[![API Docs](https://img.shields.io/badge/API%20Docs-site-e140ad.svg)](https://hmcts.github.io/reform-api-docs/swagger.html?url=https://hmcts.github.io/reform-api-docs/specs/ccd-definition-store-api.json)
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

- [Open JDK 8](https://openjdk.java.net/)
- [Docker](https://www.docker.com)

#### Environment variables

The following environment variables are required:

| Name | Default | Description |
|------|---------|-------------|
| DEFINITION_STORE_DB_USERNAME | - | Username for database |
| DEFINITION_STORE_DB_PASSWORD | - | Password for database |
| DEFINITION_STORE_DB_USE_SSL | - | set to `true` if SSL is to be enabled. `false` recommended for local environments. |
| DEFINITION_STORE_IDAM_KEY | - | Definition store's IDAM S2S micro-service secret key. This must match the IDAM instance it's being run against. |
| DEFINITION_STORE_S2S_AUTHORISED_SERVICES | ccd_data,ccd_gw,ccd_admin,jui_webapp | Authorised micro-service names for S2S calls |
| IDAM_USER_URL | - | Base URL for IdAM's User API service (idam-app). `http://localhost:4501` for the dockerised local instance or tunneled `dev` instance. |
| IDAM_S2S_URL | - | Base URL for IdAM's S2S API service (service-auth-provider). `http://localhost:4502` for the dockerised local instance or tunneled `dev` instance. |
| USER_PROFILE_HOST | - | Base URL for the User Profile service. `http://localhost:4453` for the dockerised local instance. |
| AZURE_APPLICATIONINSIGHTS_INSTRUMENTATIONKEY | - | secrets for Microsoft Insights logging, can be a dummy string in local |

### Building

The project uses [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). 

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

## LICENSE

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.


