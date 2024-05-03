#!/bin/bash

# Set terminal column width to 80
stty cols 80

# Start couchbase server
/entrypoint.sh couchbase-server &

# Path to the script that checks server availability
CHECK_AVAILABILITY_SCRIPT=/opt/couchbase/init/check_availability.sh

# Path to the file that tracks if setup is complete
FILE=/opt/couchbase/init/setupComplete.txt

# https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-cluster-init.html
initialize_cluster() {
    /opt/couchbase/bin/couchbase-cli cluster-init -c localhost \
    --cluster-name "$CLUSTER_NAME" \
    --cluster-username "$ADMINISTRATOR_USERNAME" \
    --cluster-password "$ADMINISTRATOR_PASSWORD" \
    --services data,index,query \
    --cluster-ramsize "$CLUSTER_DATA_RAM" \
    --cluster-index-ramsize "$CLUSTER_INDEX_RAM" \
    --index-storage-setting default
}

# https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-bucket-create.html
create_bucket() {
    /opt/couchbase/bin/couchbase-cli bucket-create -c localhost \
    --username "$ADMINISTRATOR_USERNAME" \
    --password "$ADMINISTRATOR_PASSWORD" \
    --bucket "$BUCKET_NAME" \
    --bucket-ramsize "$BUCKET_RAM" \
    --bucket-type couchbase \
    --enable-flush 1
}

# https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-server-add.html
add_nodes() {
    /opt/couchbase/bin/couchbase-cli server-add -c localhost \
    --username "$ADMINISTRATOR_USERNAME" \
    --password "$ADMINISTRATOR_PASSWORD" \
    --server-add "$CLUSTER_NODES" \
    --server-add-username "$ADMINISTRATOR_USERNAME" \
    --server-add-password "$ADMINISTRATOR_PASSWORD" \
    --services data,index,query
}

# https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-rebalance.html
rebalance_nodes() {
    /opt/couchbase/bin/couchbase-cli rebalance -c localhost \
    --username "$ADMINISTRATOR_USERNAME" \
    --password "$ADMINISTRATOR_PASSWORD"
}

# https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-xdcr-setup.html
setup_xdcr() {
    /opt/couchbase/bin/couchbase-cli xdcr-setup -c localhost \
    --username "$ADMINISTRATOR_USERNAME" \
    --password "$ADMINISTRATOR_PASSWORD" \
    --create \
    --xdcr-cluster-name "$XDCR_CLUSTER_NAME" \
    --xdcr-hostname "$XDCR_CLUSTER_HOSTNAME" \
    --xdcr-username "$ADMINISTRATOR_USERNAME" \
    --xdcr-password "$ADMINISTRATOR_PASSWORD"
}

# https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-xdcr-replicate.html
xdcr_replicate() {
    /opt/couchbase/bin/couchbase-cli xdcr-replicate -c localhost \
    -u "$ADMINISTRATOR_USERNAME" \
    -p "$ADMINISTRATOR_PASSWORD" \
    --create \
    --xdcr-cluster-name "$XDCR_CLUSTER_NAME" \
    --xdcr-from-bucket "$BUCKET_NAME" \
    --xdcr-to-bucket "$BUCKET_NAME"
}

orchestrate() {
    if [ ! -f "$FILE" ]; then
        if ! $CHECK_AVAILABILITY_SCRIPT; then
            exit 1
        fi

        initialize_cluster
        sleep 2s
        create_bucket
        sleep 2s

        if [ -n "$CLUSTER_NODES" ]; then
            add_nodes
            sleep 2s
            rebalance_nodes
        fi

        if [ -n "$XDCR_CLUSTER_HOSTNAME" ]; then
            setup_xdcr
            sleep 2s
            xdcr_replicate
        fi

        echo "Finished initializing cluster"
        touch $FILE
    fi

    # Keep the container running
    tail -f /dev/null
}

orchestrate
