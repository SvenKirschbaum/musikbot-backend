FROM maven:3.9.11-amazoncorretto-25@sha256:f0f3cfc7527bc7575d14ccfa889cba64bb45dd7b29cec35a7a60d2669418f779 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:25.0.1-alpine@sha256:e36ee3b9b909ea19d98d7325860bccf286ee519c50c8d33d91cfc47805ff0be7

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
