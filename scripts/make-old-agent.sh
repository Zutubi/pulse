#! /usr/bin/env bash

set -e

base="$(dirname $0)"
top="$base/.."

cd "$top"
ant -Dpulse.version=1.1.999 -Dpulse.build=0101999000 -Dskip.tests=true package.slave
cp build/pulse-agent-1.1.999.zip master/src/acceptance/data
exit 0
