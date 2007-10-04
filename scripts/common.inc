scripts="$(dirname $0)"
top="$scripts/.."
working="$top/working"

# Reports a fatal error and bails out
fatal()
{
    echo $1
    exit 1
}

# Sets up some required external service
setupService()
{
    echo "Setting up $1..."
    "$scripts/setup-$1.sh"
    echo "$1 set up."
}

# Tears down a service set up with setupService
teardownService()
{
    echo "Tearing down $1..."
    "$scripts/teardown-$1.sh"
    echo "$1 torn down."
}

# Kills a process identified by a PID in a text file, and removes the file
killPID()
{
    if [[ -f "$1" ]]
    then
        kill $(cat "$1")
        rm "$1"
    fi
}
