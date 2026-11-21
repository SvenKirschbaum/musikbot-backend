# syntax=docker/dockerfile:1.29@sha256:572cebbc2b15e6f84c077742952674889cb63cd5e9b80abdffcbe587b6f19183

FROM maven:3.10.0-amazoncorretto-25@sha256:0885bf881b6a2e62624e3c421978feca9ffde9c5b7340537ce78e2f22cc7b34a as build

ARG VERSION=dev

WORKDIR /build

COPY pom.xml .
COPY src src

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -s /root/.m2/settings.xml package -Drevision=$VERSION

FROM amazoncorretto:25.0.5-alpine@sha256:1124f698f69d2185828dbd1a1ebeb9aee21884ac64bed7af3deeafd20b21a9dc

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
