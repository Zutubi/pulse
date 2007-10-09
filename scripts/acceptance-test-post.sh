#! /usr/bin/env bash

set -e

. "$(dirname $0)/common.inc"

if [[ $# -ne 1 ]]
then
    echo "Usage: $0 <version>"
    exit 1
fi

version=$1

"$scripts/teardown-services.sh"

# Kill pulse agent and master
pushd "$working/pulse-accept"
export PULSE_HOME=
./pulse-agent-${version}/bin/pulse shutdown -p 8890 -f agent.config.properties -d agent-data
./pulse-${version}/bin/pulse shutdown -p 8889
popd

# Wait for it to shut down
while netstat -an | grep 8889 > /dev/null
do
    sleep 2
done
sleep 10

exit 0
