# syntax=docker/dockerfile:1.28@sha256:bb22d9815c728170f72750f4e5b0d672e06176142e1d602c7e66c050100b7e5b

FROM maven:3.10.0-amazoncorretto-25@sha256:d96c6ff2e7a3258e6f0c447340d2284a3404917bb8c5f01c8660cc4066bd382c as build

ARG VERSION=dev

WORKDIR /build

COPY pom.xml .
COPY src src

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -s /root/.m2/settings.xml package -Drevision=$VERSION

FROM amazoncorretto:25.0.5-alpine@sha256:b611ab31a8cff0ab60ff5ee26de46d06922bfff0c54f5029ac588d5337bc533b

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
