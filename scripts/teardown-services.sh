#! /usr/bin/env bash

set -e

. "$(dirname $0)/services-common.inc"

for service in $services
do
    teardownService $service
done

exit 0
