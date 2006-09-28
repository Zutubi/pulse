#! /bin/sh

if [ $# -ne 1 ]
then
    echo "Usage: $0 <pid file>"
    exit 1
fi

binDir=`dirname "$0"`
export PULSE_OUT="$binDir/../pulse.out"

# Launch startup in background and record PID
"$binDir"/startup.sh &
echo $! > "$1"
exit 0

