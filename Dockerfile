FROM maven:3.9.11-amazoncorretto-25@sha256:c80d6398506a1178fad0ca28ba0036cc66bdc802e613aaaf9569103fba19db26 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:25.0.1-alpine@sha256:e3818f93bee840c1593492ba5335ceb214ffe4a37a8275e49d23aab6f66b9f6a

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
