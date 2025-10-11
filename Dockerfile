FROM maven:3.9.11-amazoncorretto-25@sha256:24275bc4a714ab6f148fdcdf4d1a1207ddf7f4dba163f282f6f81a6cf9e4eeb1 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:25.0.0-alpine@sha256:e779e964a15d62c8c39dd3faa17ed2aa921795b642d4437c6c8a3ad8d581cf36

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
