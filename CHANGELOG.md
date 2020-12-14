# Changelog

All notable changes to this project will be documented in this file. 

## [Unreleased]

### Added

- Download keys service save visited countries in database.

## [1.1.0.RELEASE] - 2020-11-12

### Added

- Added documentation for simulated functionality in [README.md](./README.md).
- Added batch signature test.
- Added OWASP plugin suppresions.
- Added [efficient Docker images with Spring Boot 2.3](https://spring.io/blog/2020/08/14/creating-efficient-docker-images-with-spring-boot-2-3).

### Changed

- Fixed typo in [README.md](./README.md).
- [THIRD-PARTY-NOTICES](./THIRD-PARTY-NOTICES) - Deleted Hazelcast IMDG and Auth0 Java since these libraries are not used.
- Copied new BatchSignatureVerifier.java from EFGS.
- Upgraded to Spring Boot 2.3.5.RELEASE.
- Changed project from web application to batch application - Using [ApplicationRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/ApplicationRunner.html).

### Fixed

## [1.0.0.RELEASE] - 2020-10-31

- New service to integrate Radar COVID with [EU Federation Gateway Service (EFGS)](https://github.com/eu-federation-gateway-service/efgs-federation-gateway).