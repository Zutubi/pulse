#! /usr/bin/env bash

set -e

COMPONENTS="core local server-core master slave"

fatal()
{
    echo $*
    exit 1
}

if [[ $# -ne 1 ]]
then
    fatal "Usage: $0 <version>"
fi

version="$1"

if [[ ! "$version" =~ [1-9]+\.[0-9]+\.[0-9]+ ]]
then
    fatal "Invalid version '$version'"
fi

major=$(echo $version | cut -d. -f1)
minor=$(echo $version | cut -d. -f2)
build=$(echo $version | cut -d. -f3)

build=$(printf "%02d%02d%03d000" $major $minor $build)

tmpFile=/tmp/tmp.$$

# Update the Ivy files for all components:
#   - replace the component's status="integration" with status="release" revision="$version"
#   - replace rev="latest.integration" with rev="$version" for all pulse dependencies
for comp in $COMPONENTS
do
    
    sed -e "s/status=\"integration\"/status=\"release\" revision=\"$version\"/g" \
        -e "s/latest.integration/$version/g" $comp/ivy.xml > $tmpFile
    mv $tmpFile $comp/ivy.xml
done

# Update build.properties
sed -e "s/pulse.version=.*/pulse.version=$version/" \
    -e "s/pulse.build=.*/pulse.build=$build/" build.properties > $tmpFile
mv $tmpFile build.properties

exit 0
