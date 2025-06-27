FROM maven:3.9.10-amazoncorretto-21@sha256:54d36972c85f3a251881fe8d0d220607834519ec2640a9232856c991e2f2ad48 as build

ARG VERSION=dev

WORKDIR /build

COPY lib lib
RUN for file in ./lib/*; do mvn install:install-file -Dfile=$file; done

COPY pom.xml .
COPY src src

RUN mvn package -Drevision=$VERSION

FROM amazoncorretto:21.0.7-alpine@sha256:937a7f5c5f7ec41315f1c7238fd9ec0347684d6d99e086db81201ca21d1f5778

WORKDIR /usr/locale/musikbot-backend

COPY --from=build /build/target/backend-*.war backend.war

ENTRYPOINT ["java", "-jar", "backend.war"]
