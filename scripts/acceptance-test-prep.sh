#! /usr/bin/env bash

set -e

. "$(dirname $0)/common.inc"

if [[ $# -ne 1 ]]
then
    fatal "Usage: $0 <package>"
fi

package="$1"
agentPackage="${package/pulse/pulse-agent}"

if [[ ! -f "$package" ]]
then
    fatal "Package '$package' does not exist."
fi

if [[ ! -f "$agentPackage" ]]
then
    fatal "Agent package '$agentPackage' does not exist."
fi

"$scripts/setup-services.sh"

# Prepare a temp directory to test within
acceptDir="$working/pulse-accept"
rm -rf "$acceptDir"
mkdir "$acceptDir"

# Unpack those shiny new packages
unpack "$package" "$acceptDir"
unpack "$agentPackage" "$acceptDir"

packageName="$(basename ${package%@(.tar.gz|.zip)})"
agentPackageName="$(basename ${agentPackage%@(.tar.gz|.zip)})"

pushd "$acceptDir"
export PULSE_HOME=

# Clean up stray Firefox processes
killall firefox || true

# Start up a background agent
"./$agentPackageName/bin/pulse" start -p 8890 -f agent.config.properties -d agent-data > agent-stdout.txt 2> agent-stderr.txt &
echo $! > agent.pid

# Now start the master
"./$packageName/bin/pulse" start -p 8889 -f config.properties > stdout.txt 2> stderr.txt

trap ERR

exit 0
