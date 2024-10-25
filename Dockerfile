FROM maven:3.9.9-amazoncorretto-21@sha256:e281a9a5edc44e67259889229133ad804784906091210039a509248cf7ce067e as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.5-alpine@sha256:7ab62108b2a065f6fb42636aaf6d0b408b551d3c31c9c8a8734410abb09064ba

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
