# syntax=docker/dockerfile:1.28@sha256:bb22d9815c728170f72750f4e5b0d672e06176142e1d602c7e66c050100b7e5b

FROM maven:3.10.0-amazoncorretto-25@sha256:91d93bc90cf6f488553502ffe494a12136aca91babd8ba4caa10a4e306c0d901 as build

ARG VERSION=dev

WORKDIR /build

COPY pom.xml .
COPY src src

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -s /root/.m2/settings.xml package -Drevision=$VERSION

FROM amazoncorretto:25.0.5-alpine@sha256:abed4fe0730b802823f0cbc5c24ab8e3ba6023af99266f0e2ca3c08d9f6a20f9

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
