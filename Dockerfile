FROM maven:3.9.8-amazoncorretto-21@sha256:6bfb613ad72c84e8055566c3c908235b1218520b8d7a9ead710640419c7b22a5 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.4-alpine@sha256:4cff3d338418faa41a20bf384c7ed67b8a6897ce5ce0e3fecd7ea08c5c7a2909

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
