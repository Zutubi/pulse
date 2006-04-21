#! /bin/sh

if [[ $# -eq 1 ]]
then
    export PULSE_PID="$1"
fi

`dirname $0`/common.sh com.zutubi.pulse.command.Bootstrap start
