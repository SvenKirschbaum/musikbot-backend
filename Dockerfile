FROM maven:3.9.9-amazoncorretto-21@sha256:3ce21bfedfac11986a38cfc02431912df2d7ad294e0bfefb4d98a24b4dfdeea1 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.7-alpine@sha256:937a7f5c5f7ec41315f1c7238fd9ec0347684d6d99e086db81201ca21d1f5778

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
