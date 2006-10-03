#! /bin/sh

`dirname $0`/common.sh  -Djava.util.logging.config.class=com.zutubi.pulse.logging.ConsoleConfig com.zutubi.pulse.command.PulseCtl start $*

