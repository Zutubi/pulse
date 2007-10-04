#! /usr/bin/env bash

set -e

. "$(dirname $0)/selenium-common.inc"

if ! netstat -an | grep $port > /dev/null
then
    java -jar "$top/master/src/acceptance/misc/"selenium-server-*.jar > "$working/selenium-stdout.txt" 2> "$working/selenium-stderr.txt" &
    echo $! > "$pidfile"
fi

exit 0
