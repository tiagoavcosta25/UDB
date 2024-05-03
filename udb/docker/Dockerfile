FROM couchbase:latest AS stage_base

# Copy the scripts into the container
COPY init-cbserver.sh /opt/couchbase/init/
COPY check_availability.sh /opt/couchbase/init/

# Set execute permissions on the scripts
RUN chmod +x /opt/couchbase/init/init-cbserver.sh
RUN chmod +x /opt/couchbase/init/check_availability.sh
