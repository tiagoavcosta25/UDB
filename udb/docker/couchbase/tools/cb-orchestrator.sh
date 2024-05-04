#!/bin/bash

FLAG_FILE="/var/lib/appdata/init_done"
CLUSTER_FILE="/opt/couchbase/init/config/cluster.json"
AVAILABILITY_SCRIPT="/opt/couchbase/init/tools/check_availability.sh"

# Set terminal column width
stty cols 80

# Start Couchbase Server in the background
/entrypoint.sh couchbase-server &

load_config() {
    echo "Loading configuration from $CLUSTER_FILE..."
    # Read key-value pairs from JSON and handle arrays as comma-separated strings
    while IFS="=" read -r key value
    do
        if [[ "$value" =~ ^\[.*\]$ ]]; then
            # It's an array, remove the surrounding brackets and quotes
            clean_value=$(echo "$value" | jq -r '@csv' | tr -d '"')
            # Declare as a bash array
            IFS=',' read -r -a "${key}" <<< "$clean_value"
            declare -p "${key}"  # Optional: declare the array globally if necessary
        else
            # It's not an array, just export normally
            export "$key=$value"
        fi
    done < <(jq -r 'to_entries | .[] | "\(.key)=\(.value)"' "$CLUSTER_FILE")
}

check_config() {
    if [ -z "$BUCKET" ] || [ -z "$SCOPE" ] || [ -z "${COLLECTIONS[*]}" ]; then
        echo "Error: Configuration missing. 'BUCKET', 'SCOPE', and 'COLLECTIONS' must be set."
        exit 1
    fi
}

initialize_cluster() {
    echo "Initializing cluster..."
    /opt/couchbase/bin/couchbase-cli cluster-init -c localhost \
        --cluster-name "$CLUSTER_NAME" \
        --cluster-username "$ADMINISTRATOR_USERNAME" \
        --cluster-password "$ADMINISTRATOR_PASSWORD" \
        --services data,index,query \
        --cluster-ramsize "$CLUSTER_DATA_RAM" \
        --cluster-index-ramsize "$CLUSTER_INDEX_RAM" \
        --index-storage-setting default || exit 1
}

create_bucket() {
    echo "Creating bucket '$BUCKET'..."
    /opt/couchbase/bin/couchbase-cli bucket-create -c localhost \
        --username "$ADMINISTRATOR_USERNAME" \
        --password "$ADMINISTRATOR_PASSWORD" \
        --bucket "$BUCKET" \
        --bucket-ramsize "$BUCKET_RAM" \
        --bucket-type "$BUCKET_TYPE" \
        --enable-flush 1 || exit 1
}

create_scope() {
    echo "Creating scope..."
    /opt/couchbase/bin/couchbase-cli collection-manage -c localhost \
        --username "$ADMINISTRATOR_USERNAME" \
        --password "$ADMINISTRATOR_PASSWORD" \
        --bucket "$BUCKET" \
        --create-scope "$SCOPE" || exit 1
}

create_collections() {
    for COLLECTION in "${COLLECTIONS[@]}"; do
        create_collection "$COLLECTION"
    done
}

create_collection() {
    local COLLECTION=$1
    echo "Creating collection '$COLLECTION' in scope '$SCOPE' and bucket '$BUCKET'..."
    /opt/couchbase/bin/couchbase-cli collection-manage -c localhost \
        --username "$ADMINISTRATOR_USERNAME" \
        --password "$ADMINISTRATOR_PASSWORD" \
        --bucket "$BUCKET" \
        --create-collection "$SCOPE"."$COLLECTION" \
        --max-ttl 0 || exit 1
}

add_and_rebalance_nodes() {
    echo "Adding nodes and rebalancing..."
    /opt/couchbase/bin/couchbase-cli server-add -c localhost \
        --username "$ADMINISTRATOR_USERNAME" \
        --password "$ADMINISTRATOR_PASSWORD" \
        --server-add "$CLUSTER_NODES" \
        --server-add-username "$ADMINISTRATOR_USERNAME" \
        --server-add-password "$ADMINISTRATOR_PASSWORD" \
        --services data,index,query || exit 1

    sleep 2s  # Ensure server is added before rebalance
    /opt/couchbase/bin/couchbase-cli rebalance -c localhost \
        --username "$ADMINISTRATOR_USERNAME" \
        --password "$ADMINISTRATOR_PASSWORD" || exit 1
}

setup_xdcr_and_replicate() {
    echo "Setting up XDCR..."
    /opt/couchbase/bin/couchbase-cli xdcr-setup -c localhost \
        --username "$ADMINISTRATOR_USERNAME" \
        --password "$ADMINISTRATOR_PASSWORD" \
        --create \
        --xdcr-cluster-name "$XDCR_CLUSTER_NAME" \
        --xdcr-hostname "$XDCR_CLUSTER_HOSTNAME" \
        --xdcr-username "$ADMINISTRATOR_USERNAME" \
        --xdcr-password "$ADMINISTRATOR_PASSWORD" || exit 1

    sleep 2s  # Ensure XDCR setup is complete before replication
    echo "Starting XDCR replication..."
    /opt/couchbase/bin/couchbase-cli xdcr-replicate -c localhost \
        -u "$ADMINISTRATOR_USERNAME" \
        -p "$ADMINISTRATOR_PASSWORD" \
        --create \
        --xdcr-cluster-name "$XDCR_CLUSTER_NAME" \
        --xdcr-from-bucket "$BUCKET" \
        --xdcr-to-bucket "$BUCKET" || exit 1
}

orchestrate() {
    if [ ! -f "$FLAG_FILE" ]; then
        load_config
        check_config

        $AVAILABILITY_SCRIPT || exit 1

        initialize_cluster
        create_bucket
        create_scope
        create_collections

        [ -n "$CLUSTER_NODES" ] && add_and_rebalance_nodes
        [ -n "$XDCR_CLUSTER_HOSTNAME" ] && setup_xdcr_and_replicate

        echo "Cluster initialization complete."
        # both dc's doing this, only main one should probably / app should wait for 2 files
        if [ "$MAIN" = "true" ]; then
          touch $FLAG_FILE
        fi
    else
        echo "Cluster already initialized."
    fi

    # Persist the container run
    tail -f /dev/null
}

orchestrate
