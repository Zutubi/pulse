#! /usr/bin/env bash

set -e

usage()
{
    echo "Usage: $0 [ <option>... ] <version>"
    echo "       $0 -h"
    echo "Options:"
    echo "   -u <subversion user>: provide an alternative user name to subversion"
    echo "   -y                  : automatically answer yes to any queries"
}

yesNo()
{
    if [[ -z "$yesToAll" ]]
    then
        read -p "$1" answer
        if [[ $answer != y && $answer != Y ]]
        then
            echo "Aborting."
            exit 1
        fi
    fi
}

fatal()
{
    echo "$1"
    exit 1
}

while getopts "hu:y" options; do
    case $options in
        h ) usage
            exit 0;;
        u ) user=$OPTARG;;
        y ) yesToAll=y;;
        \? ) usage
             exit 1;;
        * ) usage
            exit 1;;
    esac
done

shift $(($OPTIND - 1))

if [[ $# -ne 1 ]]
then
    usage
    exit 1
fi

version="$1"
if [[ ! "$version" =~ [1-9]+\.[0-9]+\.[0-9]+ ]]
then
    fatal "Invalid version '$version'"
fi

if [[ ! -z "$user" ]]
then
    user="${user}@"
fi

svnBase="svn+ssh://${user}cinnamonbob.com/svnroot/pulse"
svnTag="$svnBase/tags/$version"

#if svn info "$svnTag" > /dev/null 2>& 1
#then
#    yesNo "Tag $version already exists.  Use existing [yN]? "
#else
    svn copy -m "Tagging release $version" "$svnBase/trunk" "$svnTag"
#fi

tmpDir=checkout.$$

yesNo "Build release now [yN]? "

svn co $svnTag $tmpDir
pushd $tmpDir
./build-release.sh $version

echo "========================================================================="
echo "= Changes on tag $version"
echo "========================================================================="

svn st -q

echo "========================================================================="

yesNo "Commit and continue [yN]? "
svn commit -m "Updated ivy files and build.properties for $version release"
popd

rm -rf $tmpDir

exit 0
