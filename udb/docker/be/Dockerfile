# Using an official Amazon Corretto 17 Alpine-based image
FROM openjdk:17-alpine

LABEL maintainer="Couchbase"

WORKDIR /app

COPY docker/be/wait-for.sh /usr/local/bin/wait-for.sh
RUN chmod +x /usr/local/bin/wait-for.sh

# Alpine Linux uses apk for package management. Add basic tools and Maven.
RUN apk update && apk add --no-cache \
    maven \
    jq \
    curl

# Adding application source code to the container
ADD . /app

# Install project dependencies and generate jar file
RUN mvn clean install
