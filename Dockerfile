FROM maven:3.9.6-amazoncorretto-21@sha256:163c98495b9d4406d956135cf6e8bb5fd018fa14717b412643820c7434811808 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.2-alpine@sha256:cc4c1d0cab18894f4470b3afda995fbda8b5166d9d646205a18357b2b20c4b2b

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
