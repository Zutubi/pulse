#! /usr/bin/env bash

set -e

. "$(dirname $0)/xvfb-common.inc"

Xvfb :$display -ac &
echo $! > "$pidfile"

exit 0
