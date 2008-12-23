#! /usr/bin/env bash

set -e

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

if [[ "$version" =~ [1-9]+\.[0-9]+\.[0-9]+ ]]
then
    major=$(echo $version | cut -d. -f1)
    minor=$(echo $version | cut -d. -f2)
    build=$(echo $version | cut -d. -f3)
    patch="0"
elif [[ "$version" =~ [1-9]+\.[0-9]+\.[0-9]+\.[0-9]+ ]]
    major=$(echo $version | cut -d. -f1)
    minor=$(echo $version | cut -d. -f2)
    build=$(echo $version | cut -d. -f3)
    patch=$(echo $version | cut -d. -f4)
else
    fatal "Invalid version '$version'"
fi


build=$(printf "%02d%02d%03d%03d" $major $minor $build $patch)

tmpFile=/tmp/tmp.$$

# Update the Ivy files for all components:
#   - replace the component's status="integration" with status="release" revision="$version"
#   - replace rev="latest.integration" with rev="$version" for all pulse dependencies
for ivy in */ivy.xml package/*/ivy.xml bundles/*/ivy.xml
do
    sed -e "s/status=\"integration\"/status=\"release\" revision=\"$version\"/g" \
        -e "s/latest.integration/$version/g" $ivy > $tmpFile
    mv $tmpFile $ivy
done

for manifest in bundles/*/resources/META-INF/MANIFEST.MF
do
    sed -e "s/Bundle-Version: [0-9]*\.[0-9]*\.[0-9]*/Bundle-Version: $version/g" $manifest > $tmpFile
    mv $tmpFile $manifest
done

# Update build.properties
sed -e "s/pulse.version=.*/pulse.version=$version/" \
    -e "s/pulse.build=.*/pulse.build=$build/" build.properties > $tmpFile
mv $tmpFile build.properties

exit 0
