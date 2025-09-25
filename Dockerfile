FROM maven:3.9.11-amazoncorretto-21@sha256:8bb189a6673bb8b674af267ead6037e2d0508c950daac45c3360e57093476c07 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:25.0.0-alpine@sha256:807ea3c4000a052986cd1e7097a883f9cd7a6e527f73841f462e3d04851b8835

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
