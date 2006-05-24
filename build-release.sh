#! /usr/bin/env bash

set -e

COMPONENTS="boot core local server-core master slave"

fatal()
{
    echo $*
    exit 1
}

if [[ $# -ne 2 ]]
then
    fatal "Usage: $0 <build number> <version>"
fi

build=$1
version=$2

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

# Build that mofo
ant clean.all accept.master

echo "If you be happy, svn commit what I hath wrought."

exit 0
