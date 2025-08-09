FROM maven:3.9.11-amazoncorretto-21@sha256:a3426009321cda086f4883a6aa6e3aa84e05cdd8bf867a2a1b8d039e93dfdccf as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.8-alpine@sha256:fda60fd7965970ce7ed7ce789b18418647b56ac6112fc17df006337bdc6355c4

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
