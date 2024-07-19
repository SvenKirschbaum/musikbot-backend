FROM maven:3.9.8-amazoncorretto-21@sha256:5625f89880deb3bac2d7122339fc106684effd0c8ad683152ff9e0431290c53a as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.4-alpine@sha256:88ca0023ea680ee8f2da87f30abef7303d1320ee9c9655a93e386aaba81647fa

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
