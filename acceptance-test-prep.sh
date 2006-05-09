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
trap "rm -rf $tmpDir" ERR

# Unpack that shiny new package
extension="${package##*.}"
if [[ $extension == "tgz" ]]
then
    tar -xv -C $tmpDir -f "$package"
else
    unzip -d $tmpDir "$package"
fi

# Set the webapp port to something less clashworthy
packageName="$(basename $package .$extension)"
pushd "$tmpDir/$packageName"
properties=system/config/pulse-defaults.properties
sed -e 's/8080/8889/g' $properties > $properties.$$
mv $properties.$$ $properties

# Fire it up!
export PULSE_HOME="$(pwd)"
$(pwd)/bin/startup.sh > /cygdrive/c/pulse.log

trap ERR

exit 0
