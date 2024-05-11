FROM openjdk:17-alpine AS stage_base

LABEL maintainer="costat2"

WORKDIR /app

COPY docker/be/wait-for.sh /usr/local/bin/wait-for.sh
RUN chmod +x /usr/local/bin/wait-for.sh

RUN apk update && apk add --no-cache \
    maven \
    jq \
    curl

ADD . /app

COPY docker/be/libs/ /app/libs/

RUN mvn install:install-file \
    -Dfile=libs/framework-1.0-SNAPSHOT.jar \
    -DgroupId=org.socialnetwork \
    -DartifactId=framework \
    -Dversion=1.0-SNAPSHOT \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN mvn clean install
