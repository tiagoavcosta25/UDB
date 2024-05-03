#!/bin/bash
# used to start couchbase server - can't get around this as docker compose only allows you to start one command - so we have to start couchbase like the standard couchbase Dockerfile would
# https://github.com/couchbase/docker/blob/master/enterprise/couchbase-server/7.0.3/Dockerfile#L82

# sets terminal column width to 80
stty cols 80

/entrypoint.sh couchbase-server &

# track if setup is complete so we don't try to setup again
FILE=/opt/couchbase/init/setupComplete.txt

CHECK_AVAILABILITY_SCRIPT=/opt/couchbase/init/check_availability.sh

if ! [ -f "$FILE" ]; then

  if ! $CHECK_AVAILABILITY_SCRIPT; then
    exit 1
  fi

  # used to automatically create the cluster based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-cluster-init.html

  /opt/couchbase/bin/couchbase-cli cluster-init -c localhost \
  --cluster-name "$CLUSTER_NAME" \
  --cluster-username "$ADMINISTRATOR_USERNAME" \
  --cluster-password "$ADMINISTRATOR_PASSWORD" \
  --services data,index,query \
  --cluster-ramsize "$CLUSTER_DATA_RAM" \
  --cluster-index-ramsize "$CLUSTER_INDEX_RAM" \
  --index-storage-setting default

   sleep 2s

  # used to auto create the bucket based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-bucket-create.html

  /opt/couchbase/bin/couchbase-cli bucket-create -c localhost \
  --username "$ADMINISTRATOR_USERNAME" \
  --password "$ADMINISTRATOR_PASSWORD" \
  --bucket "$BUCKET_NAME" \
  --bucket-ramsize "$BUCKET_RAM" \
  --bucket-type couchbase \
  --enable-flush 1

  sleep 2s

  # used to add nodes to the cluster
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-server-add.html

  # -z checks if string is empty
  if [ -z "$CLUSTER_NODES" ]; then
    echo "No additional nodes for cluster $CLUSTER_NAME"
  else
    /opt/couchbase/bin/couchbase-cli server-add -c localhost \
    --username "$ADMINISTRATOR_USERNAME" \
    --password "$ADMINISTRATOR_PASSWORD" \
    --server-add "$CLUSTER_NODES" \
    --server-add-username "$ADMINISTRATOR_USERNAME" \
    --server-add-password "$ADMINISTRATOR_PASSWORD" \
    --services data,index,query
  fi

  sleep 2s

  # used to rebalance nodes
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-rebalance.html

  if [ -n "$CLUSTER_NODES" ]; then
    /opt/couchbase/bin/couchbase-cli rebalance -c localhost \
    --username "$ADMINISTRATOR_USERNAME" \
    --password "$ADMINISTRATOR_PASSWORD"
  fi

  sleep 2s

  # used to setup xdcr
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-xdcr-setup.html

  if [ -n "$XDCR_CLUSTER_HOSTNAME" ]; then
    /opt/couchbase/bin/couchbase-cli xdcr-setup -c localhost \
    --username "$ADMINISTRATOR_USERNAME" \
    --password "$ADMINISTRATOR_PASSWORD" \
    --create \
    --xdcr-cluster-name "$XDCR_CLUSTER_NAME" \
    --xdcr-hostname "$XDCR_CLUSTER_HOSTNAME" \
    --xdcr-username "$ADMINISTRATOR_USERNAME" \
    --xdcr-password "$ADMINISTRATOR_PASSWORD"
  fi

  sleep 2s

  # used to xdcr replicate
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-xdcr-replicate.html
  if [ -n "$XDCR_CLUSTER_HOSTNAME" ]; then
    couchbase-cli xdcr-replicate -c localhost \
    -u Administrator \
    -p password \
    --create \
    --xdcr-cluster-name "$XDCR_CLUSTER_NAME" \
    --xdcr-from-bucket "$BUCKET_NAME" \
    --xdcr-to-bucket "$BUCKET_NAME"
  fi

  echo "Finished initializing cluster"

  # https://docs.couchbase.com/server/current/manage/manage-scopes-and-collections/manage-scopes-and-collections.html

  # create file so we know that the cluster is setup and don't run the setup again
  touch $FILE
fi
  # docker compose will stop the container from running unless we do this
  # known issue and workaround
  tail -f /dev/null
