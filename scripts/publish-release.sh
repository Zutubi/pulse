#! /usr/bin/env bash

set -e
set -u

SCRATCH=/tmp/scratch-$$
SSH_CTL=$SCRATCH/sshctl

cleanup() {
    if [[ -f $SSH_CTL ]]
    then
        ssh -o ControlPath=$SSH_CTL -O exit zutubi.com
    fi

    rm -rf $SCRATCH
}

mkdir $SCRATCH
trap cleanup INT TERM EXIT

if [[ $# -lt 1 ]]
then
    echo "Usage: $0 <release stream>"
    echo "e.g. $0 2.5"
    exit 1
fi

RELEASE=$1
RELEASE_DIR="/var/pulse2/projects/pulse $RELEASE release"
if [[ ! -d "$RELEASE_DIR" ]]
then
    echo "Could not find release stream $RELEASE."
    exit 1
fi

BUILD_DIR="$RELEASE_DIR"/$(ls -1 "$RELEASE_DIR" |grep '^0' |tail -1)
OUTPUT_DIR=$(echo "$BUILD_DIR"/[0-9]*/output)
if [[ ! -d "$OUTPUT_DIR" ]]
then
    echo "Could not find output directory for recipe."
    exit 1
fi

PACKAGES_DIR="$OUTPUT_DIR/00000002-build/packages"
EXE=$(echo "$PACKAGES_DIR/"pulse-$RELEASE.[0-9]*.exe)
if [[ ! -f "$EXE" ]]
then
    echo "Could not find exe package."
    exit 1
fi

EXE_BASE=$(basename "$EXE")
BUILD=${EXE_BASE#pulse-$RELEASE.}
BUILD=${BUILD%.exe}

read -p "Latest build is $BUILD.  Publish [y/N]? " RESPONSE
if [[ "$RESPONSE" != y && "$RESPONSE" != Y ]]
then
    echo "OK, aborting."
    exit 2
fi

echo "Opening connection to zutubi.com..."
CTL="-o ControlPath=$SSH_CTL"
ssh -NfM $CTL root@zutubi.com
echo "Connection open."

echo "Publishing packages..."
scp $CTL "$PACKAGES_DIR"/pulse-* root@zutubi.com:/var/www/zutubi.com
echo "Packages published."

echo "Publishing javadoc..."
echo "  Packing..."
cd "$OUTPUT_DIR/00000004-javadoc"
ARCHIVE=$SCRATCH/javadoc.tgz
tar zcf $ARCHIVE javadoc
echo "  Uploading..."
scp $CTL $ARCHIVE root@zutubi.com:/var/www/doc/pulse
echo "  Unpacking and moving into place..."
ssh $CTL root@zutubi.com "cd /var/www/doc/pulse; tar zxf javadoc.tgz; mv javadoc $RELEASE.$BUILD; rm -f $RELEASE; ln -s $RELEASE.$BUILD $RELEASE; chown -R www-data:www-data $RELEASE $RELEASE.$BUILD; rm javadoc.tgz"
echo "Javadoc published."

echo "Updating Ivy repository..."
ssh $CTL root@zutubi.com "cd /var/www/ivy; svn up"
echo "Ivy updated."

echo "Release $RELEASE build $BUILD published!"

cleanup
trap - INT TERM EXIT
exit 0

