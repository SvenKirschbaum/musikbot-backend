FROM maven:3.9.16-amazoncorretto-25@sha256:10a4c5d155c12014589a4d474901c36f1258f3f066fea2317a7722f42c6a2a25 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:25.0.3-alpine@sha256:32d81edae73e1670244827c2f12e5bcf0d335f035b538455fe9d02eb0771d41b

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
