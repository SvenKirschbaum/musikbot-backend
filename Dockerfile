FROM maven:3.9.15-amazoncorretto-25@sha256:5d270f238645533767a660ec6747c98a8d6b269b8f0dd170ccd02ab14f136d29 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:25.0.3-alpine@sha256:80667e38af71ac103a3ae36a0b531d54c73c4da28fc02b57f69bce8993c0e1b0

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
