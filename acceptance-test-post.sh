#! /usr/bin/env bash

set -e

if [[ $# -ne 1 ]]
then
    echo "Usage: $0 <version>"
    exit 1
fi

version=$1
./pulse-accept/pulse-${version}/bin/shutdown.sh

# Wait for it to shut down
while netstat -a | grep 8889 > /dev/null
do
    sleep 2
done
sleep 10

exit 0
