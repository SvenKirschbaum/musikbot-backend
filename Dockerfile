# syntax=docker/dockerfile:1.27@sha256:4edf897a3ffa55b89f906fc8cc78afdb3f1834cc9c7083565e611a8a7d5fe99e

FROM maven:3.10.0-amazoncorretto-25@sha256:bf28f1ea992519d0c6a94e970b64ef97a3398795be8081f86ebd66786f94abdd as build

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
