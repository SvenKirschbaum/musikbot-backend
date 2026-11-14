# syntax=docker/dockerfile:1.29@sha256:572cebbc2b15e6f84c077742952674889cb63cd5e9b80abdffcbe587b6f19183

FROM maven:3.10.0-amazoncorretto-25@sha256:cd70d2558213140e9352b3ef0badc40a09140e7e48f72e78a3760da5fe614665 as build

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
