#! /usr/bin/env bash

set -e

. "$(dirname $0)/services-common.inc"

for service in $services
do
    setupService $service
done

exit 0
