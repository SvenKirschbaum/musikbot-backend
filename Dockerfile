# syntax=docker/dockerfile:1.26@sha256:ecfaec9ed6d810b56388c508f4121597bfbba70d41a6dfeee4d8cad5f295fc32

FROM maven:3.9.16-amazoncorretto-25@sha256:de7a3e517efac1b933af6ceb375974a061ba71c908ea51a18bd937716a8ade93 as build

ARG VERSION=dev

WORKDIR /build

COPY pom.xml .
COPY src src

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -s /root/.m2/settings.xml package -Drevision=$VERSION

FROM amazoncorretto:25.0.4-alpine@sha256:027310590da693629c2cf704d2f87e9359c33ee2f02bcaa777680b2f4b94f4c7

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
