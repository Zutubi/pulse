#! /usr/bin/env bash

set -e

DESC="build server"
NAME="pulse"

fatal()
{
    echo $1
    exit 1
}


if [[ -z "$PULSE_HOME" ]]
then
    fatal "PULSE_HOME must be set."
fi

if [[ -z "$JAVA_HOME" ]]
then
    fatal "JAVA_HOME must be set."
fi

if [[ -z "$PULSE_USER" ]]
then
    PULSE_USER=root
fi

if [[ -z "$PULSE_PID" ]]
then
    PULSE_PID=/var/run/pulse.pid
fi


d_start()
{
    su $PULSE_USER -c "'$PULSE_HOME/bin/startup.sh' '$PULSE_PID'"
}

d_stop()
{
    su $PULSE_USER -c "'$PULSE_HOME/bin/shutdown.sh' --force"
    if [[ -f "$PULSE_PID" ]]
    then
        pid=$(cat "$PULSE_PID")
        
        count=0
        until [[ $(ps --pid $pid | grep -c $pid) = "0" ]] || [[ $count -gt 30 ]]
        do
            sleep 1
            count=$((count + 1))
        done

        if [[ $count -gt 30 ]]
        then
            kill -9 $pid
        fi
    fi

    rm -f "$PULSE_PID"
}

case "$1" in
  start)
        echo -n "Starting $DESC: $NAME"
        d_start
        echo "."
        ;;
  stop)
        echo -n "Stopping $DESC: $NAME"
        d_stop
        echo "."
        ;;
  restart|force-reload)
        echo -n "Restarting $DESC: $NAME"
        d_stop
        sleep 1
        d_start
        echo "."
        ;;
  *)
        echo "Usage: $SCRIPTNAME {start|stop|restart|force-reload}" >&2
        exit 1
        ;;
esac

exit 0
