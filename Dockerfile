FROM maven:3.9.6-amazoncorretto-21@sha256:3bd42e6662994d1168abb78460cd549b7dc85f58f56978128afc1e54ceffc497 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.2-alpine@sha256:9d4583a31932f23f12b0a9955c6b0518439fbb7e35d2a333c2ea46a7e91024a2

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
