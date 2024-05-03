FROM couchbase:latest AS stage_base

# Copy the scripts into the container
COPY tools/cb-orchestrator.sh /opt/couchbase/init/
COPY tools/check_availability.sh /opt/couchbase/init/

# Set execute permissions on the scripts
RUN chmod +x /opt/couchbase/init/cb-orchestrator.sh
RUN chmod +x /opt/couchbase/init/check_availability.sh
