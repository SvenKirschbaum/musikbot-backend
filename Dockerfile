FROM maven:3.9.9-amazoncorretto-21@sha256:fbb6d3f2590f9769b802bb4cc2d08968240112ce0b28c97668bad8215bf296a9 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.6-alpine@sha256:1b53a05c5693b5452a0c41a39b1fa3b8e7d77aa37f325acc378b7928bc1d8253

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
