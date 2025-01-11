FROM maven:3.9.9-amazoncorretto-21@sha256:17ae7b5533254592b8ab1a159cdb63777a692eab49754b708711854c0a68d6a4 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.5-alpine@sha256:b66d1d797ca711a537667d22aea4713577338bd161447c91efaa61a8e6855981

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
