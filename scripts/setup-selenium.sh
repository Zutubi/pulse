#! /usr/bin/env bash

set -e

. "$(dirname $0)/selenium-common.inc"

if ! netstat -an | grep \\b$port\\b > /dev/null
then
    java -jar "$top/com.zutubi.pulse.acceptance/src/test/misc/"selenium-server-*.jar -port $port > "$working/selenium-stdout.txt" 2> "$working/selenium-stderr.txt" &
    echo $! > "$pidfile"
fi

exit 0
