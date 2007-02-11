#! /usr/bin/env bash

set -e

if [[ $# -ne 1 ]]
then
    echo "Usage: $0 <version>"
    exit 1
fi

version=$1
export PULSE_HOME=
./pulse-accept/pulse-${version}/bin/pulse shutdown -p 8889

# Wait for it to shut down
while netstat -an | grep 8889 > /dev/null
do
    sleep 2
done
sleep 10

exit 0
