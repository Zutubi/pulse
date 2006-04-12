#! /usr/bin/env bash

set -e

if [[ $# -ne 1 ]]
then
    echo "Usage: $0 <version>"
    exit 1
fi

version=$1
./pulse-accept/pulse-${version}/bin/shutdown.sh
rm -rf pulse-accept

exit 0
