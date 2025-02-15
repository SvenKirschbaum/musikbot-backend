FROM maven:3.9.9-amazoncorretto-21@sha256:4c47e9a5e4f1c61216aebe524891948d74a6955e0097774a161a64297d8e1ba5 as build

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
