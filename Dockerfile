FROM maven:3.9.6-amazoncorretto-21@sha256:f004b534b13f0a785ad0a904ed012606b0939b40344939fe7e4cf1be9a3f92e5 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.2-alpine@sha256:ecdb53d62a45cb978b849e30ebcc16933ad5c2a7659b94f0c556b93fe575cda9

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
