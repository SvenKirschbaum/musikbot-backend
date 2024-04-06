FROM maven:3.9.6-amazoncorretto-21@sha256:3a69a5d9c136b0d8812f45bf07ca3830dd19908126a452fefcd51dab19c8e658 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.2-alpine@sha256:b2057d2b22c9abf559ea3c0334a161fb0e615bbcf27713be2b754fd2ca804526

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
