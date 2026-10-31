# syntax=docker/dockerfile:1.28@sha256:bb22d9815c728170f72750f4e5b0d672e06176142e1d602c7e66c050100b7e5b

FROM maven:3.10.0-amazoncorretto-25@sha256:ccc394dd4226c146c2e81f68b3d95e68227e9ffaa88fe04b9f22fd6552190e0b as build

ARG VERSION=dev

WORKDIR /build

COPY pom.xml .
COPY src src

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -s /root/.m2/settings.xml package -Drevision=$VERSION

FROM amazoncorretto:25.0.5-alpine@sha256:e3ec06bdc025f54f1dbeeb92a8cc537665371badec999ce832c1cb6c00a2c828

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
