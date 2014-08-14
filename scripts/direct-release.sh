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

EXE=$(echo build/pulse-[0-9]*.exe)
EXE_BASE=$(basename "$EXE")
VERSION=${EXE_BASE#pulse-}
VERSION=${VERSION%.exe}
RELEASE=${VERSION%.*}

read -p "Release is $RELEASE, version is $VERSION.  Publish [y/N]? " RESPONSE
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
scp $CTL build/pulse-* root@zutubi.com:/var/www/zutubi.com
echo "Packages published."

echo "Publishing javadoc..."
echo "  Packing..."
cd build/docs
ARCHIVE=$SCRATCH/javadoc.tgz
tar zcf $ARCHIVE javadoc
echo "  Uploading..."
scp $CTL $ARCHIVE root@zutubi.com:/var/www/doc/pulse
echo "  Unpacking and moving into place..."
ssh $CTL root@zutubi.com "cd /var/www/doc/pulse; tar zxf javadoc.tgz; mv javadoc $VERSION; rm -f $RELEASE; ln -s $VERSION $RELEASE; chown -R www-data:www-data $RELEASE $VERSION; rm javadoc.tgz"
echo "Javadoc published."

echo "Updating Ivy repository..."
ssh $CTL root@zutubi.com "cd /var/www/ivy; svn up"
echo "Ivy updated."

echo "Release $VERSION published!"

cleanup
trap - INT TERM EXIT
exit 0

