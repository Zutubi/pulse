#! /usr/bin/env bash

set -e

base="$(dirname $0)"
top="$base/.."

cd "$top"
ant -Dpulse.version=2.0.0 -Dpulse.build=0200000000 -Dskip.tests=true clean.all package.slave
cp build/pulse-agent-2.0.0.tar.gz acceptance/src/test/data
exit 0
