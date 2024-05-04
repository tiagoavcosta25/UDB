FROM couchbase:latest AS stage_base

LABEL maintainer="costat2"

COPY config/ /opt/couchbase/init/config
COPY tools/ /opt/couchbase/init/tools

RUN chmod +x /opt/couchbase/init/tools/*.sh

RUN apt-get update && apt-get install -y jq && rm -rf /var/lib/apt/lists/*
