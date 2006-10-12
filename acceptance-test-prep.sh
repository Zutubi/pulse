#! /usr/bin/env bash

set -e

# Reports a fatal error and bails out
fatal()
{
    echo $1
    exit 1
}

if [[ $# -ne 1 ]]
then
    fatal "Usage: $0 <package>"
fi

package="$1"

if [[ ! -f "$package" ]]
then
    fatal "Package '$package' does not exist."
fi

# Prepare a temp directory to test within
tmpDir=pulse-accept
rm -rf $tmpDir
mkdir $tmpDir

# Unpack that shiny new package
extension="${package##*.}"
if [[ $extension == "gz" ]]
then
    tar -xv -C $tmpDir -f "$package"
    extension=tar.gz
else
    unzip -d $tmpDir "$package"
fi

packageName="$(basename $package .$extension)"
pushd "$tmpDir/$packageName"

# Fire it up!
export PULSE_HOME=
"$(pwd)/bin/pulse" start -p 8889 -f config.properties > ../stdout.txt 2> ../stderr.txt

trap ERR

exit 0
