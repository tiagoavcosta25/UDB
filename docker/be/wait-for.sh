#!/bin/sh
# wait-for.sh

FILE_PATH=$1
shift
cmd="$@"

until [ -f $FILE_PATH ]; do
  >&2 echo "Waiting for file - $FILE_PATH"
  sleep 15
done

>&2 echo "File $FILE_PATH found. Executing command."
exec $cmd