FROM openjdk:17-alpine

LABEL maintainer="costat2"

WORKDIR /app

COPY docker/be/wait-for.sh /usr/local/bin/wait-for.sh
RUN chmod +x /usr/local/bin/wait-for.sh

RUN apk update && apk add --no-cache \
    maven \
    jq \
    curl
