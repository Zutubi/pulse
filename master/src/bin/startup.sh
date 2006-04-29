#! /bin/sh

if [[ $# -eq 1 ]]
then
    export PULSE_PID="$1"
fi

`dirname $0`/common.sh  -Djava.util.logging.config.class=com.zutubi.pulse.logging.ConsoleConfig com.zutubi.pulse.command.Bootstrap start
