FROM maven:3.9.12-amazoncorretto-25@sha256:9af549434620b4f03470e96d5f8432ad15c3a336b427d1c6188a7cad7d7ae933 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:25.0.2-alpine@sha256:241d36b913a364566890afd6f7945c2be23fb3f05968c920f5dccafac9c2bca2

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
