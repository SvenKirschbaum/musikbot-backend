# syntax=docker/dockerfile:1.28@sha256:bb22d9815c728170f72750f4e5b0d672e06176142e1d602c7e66c050100b7e5b

FROM maven:3.10.0-amazoncorretto-25@sha256:da0f0354bbe233537c3bf33cdf907536ae9b178dfd7c41e3eb9f815b7f8f14c9 as build

ARG VERSION=dev

WORKDIR /build

COPY pom.xml .
COPY src src

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -s /root/.m2/settings.xml package -Drevision=$VERSION

FROM amazoncorretto:25.0.4-alpine@sha256:19f1e2198abaaf201f5b9faa39222412da3fad66415e9dfe253bd6763415097e

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
