FROM maven:3.9.9-amazoncorretto-21@sha256:92aba821b989f5863d55f65ccee20f61161e482a8e4555997e94288e584936c0 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.5-alpine@sha256:8b16834e7fabfc62d4c8faa22de5df97f99627f148058d52718054aaa4ea3674

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
