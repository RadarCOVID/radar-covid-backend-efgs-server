# RadarCOVID EFGS Service

<p align="center">
    <a href="https://github.com/RadarCOVID/radar-covid-backend-efgs-server/commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/RadarCOVID/radar-covid-backend-efgs-server?style=flat"></a>
    <a href="https://github.com/RadarCOVID/radar-covid-backend-efgs-server/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/RadarCOVID/radar-covid-backend-efgs-server?style=flat"></a>
    <a href="https://github.com/RadarCOVID/radar-covid-backend-efgs-server/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg?style=flat"></a>
</p>

## Introduction

EFGS Service in terms of the Radar COVID project enables the connectivity with [EU Federation Gateway Service (EFGS)](https://github.com/eu-federation-gateway-service/efgs-federation-gateway).

## Prerequisites

These are the frameworks and tools used to develop the solution:

- [Java 11](https://openjdk.java.net/).
- [Maven](https://maven.apache.org/).
- [Spring Boot](https://spring.io/projects/spring-boot) version 2.3.
- [Lombok](https://projectlombok.org/), to help programmer. Developers have to include the IDE plugin to support Lombok features (ie, for Eclipse based IDE, go [here](https://projectlombok.org/setup/eclipse)).
- [ArchUnit](https://www.archunit.org/) is used to check Java architecture.
- [PostgreSQL](https://www.postgresql.org/).
- Testing:
    - [Spock Framework](http://spockframework.org/).
    - [Docker](https://www.docker.com/), because of using Testcontainers.
    - [Testcontainers](https://www.testcontainers.org/).
- Monitoring:
    - [Micrometer](https://micrometer.io/).

## Installation and Getting Started

### Building from Source

To build the project, you need to run this command:

```shell
mvn clean package -P<environment>
```

Where `<environment>` has these possible values:

- `local-env`. To run the application from local (eg, from IDE o from Maven using `mvn spring-boot:run`). It is the default profile, using [`application-local.yml`](./efgs-server-boot/src/main/resources/application-local.yml) configuration file.
- `docker-env`. To run the application in a Docker container with `docker-compose`, using [`application-docker.yml`](./efgs-server-boot/src/main/resources/application-docker.yml) configuration file.
- `pre-env`. To run the application in the Preproduction environment, using [`application-pre.yml`](./efgs-server-boot/src/main/resources/application-pre.yml) configuration file.
- `pro-env`. To run the application in the Production environment, using [`application-pro.yml`](./efgs-server-boot/src/main/resources/application-pro.yml) configuration file.

The project also uses Maven profile `aws-env` to include dependencies when it is running on AWS environment, so the compilation command for Preproduction and Production environments would be:

```shell
mvn clean package -P pre-env,aws-env
mvn clean package -P pro-env,aws-env
```

All profiles will load the default [configuration file](./efgs-server-boot/src/main/resources/application.yml).

### Running the Project

Depends on the environment you selected when you built the project, you can run the project:

- From the IDE, if you selected `local` environment (or you didn't select any Maven profile).
- From Docker. Once you build the project, you will have in `efgs-server-boot/target/docker` the files you would need to run the application from a container (`Dockerfile` and the Spring Boot fat-jar).

If you want to run the application inside a docker in local, once you built it, you should run:

```shell
docker-compose up -d postgres
docker-compose up -d backend
```

#### EFGS Federation Gateway service

There are two ways to connect with [EFGS Federation Gateway](https://github.com/eu-federation-gateway-service/efgs-federation-gateway) service:

- Normal. This option provides a connection with the third party service EFGS Gateway. It's necessary to provides gateway urls, so if you up gateway service in local you must change urls on [`application.yml`](./efgs-server-boot/src/main/resources/application.yml).

```shell
application.efgs.upload-diagnosis-keys.url: localhost:<port>/diagnosiskeys/upload
application.efgs.download-diagnosis-keys.download.url: localhost:<port>/diagnosiskeys/download
application.efgs.download-diagnosis-keys.audit.url: localhost:<port>/diagnosiskeys/audit/download
```

- Simulate. This option returns mocks values and actually doesn't connect with third party services. To enable this option it's necessary to add the following properties on [`application.yml`](./efgs-server-boot/src/main/resources/application.yml).

```shell
application.efgs.upload-diagnosis-keys.simulate: true
application.efgs.download-diagnosis-keys.simulate: true
```

#### Database

This project doesn't use either [Liquibase](https://www.liquibase.org/) or [Flyway](https://flywaydb.org/) because:

1. DB-Admins should only have database privileges to maintain the database model ([DDL](https://en.wikipedia.org/wiki/Data_definition_language)).
2. Applications should only have privileges to maintain the data ([DML](https://en.wikipedia.org/wiki/Data_manipulation_language)).

We use the same data model as DP3T but include some changes to support EFGS integration. To be able to launch EFGS integration on local environment we copied the SQL files from D3PT and add these 2 files:

- [`V0_8__gaen_efgs.sql`](./sql/V0_8__gaen_efgs.sql). Script to add columns to `t_gaen_exposed` table.
- [`V0_9__ddl_batch.sql`](./sql/V0_9__ddl_batch.sql). Script to add tables needed to support batch processes (upload and download batches).

### Modules

EFGS Service has four modules:

- `efgs-server-parent`. Parent Maven project to define dependencies and plugins.
- `efgs-server-api`. [DTOs](https://en.wikipedia.org/wiki/Data_transfer_object) exposed.
- `efgs-server-boot`. Main application, global configurations and properties. This module also has integration tests and Java architecture tests with ArchUnit:
- `efgs-server-service`. Business and data layers.

## Support and Feedback
The following channels are available for discussions, feedback, and support requests:

| Type       | Channel                                                |
| ---------- | ------------------------------------------------------ |
| **Issues** | <a href="https://github.com/RadarCOVID/radar-covid-backend-efgs-server/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/RadarCOVID/radar-covid-backend-efgs-server?style=flat"></a> |

## Contribute

If you want to contribute with this exciting project follow the steps in [How to create a Pull Request in GitHub](https://opensource.com/article/19/7/create-pull-request-github).

More details in [CONTRIBUTING.md](./CONTRIBUTING.md).

## License

This Source Code Form is subject to the terms of the [Mozilla Public License, v. 2.0](https://www.mozilla.org/en-US/MPL/2.0/).

