#! /usr/bin/env bash

set -e

usage()
{
    echo "Usage: $0 [ <option>... ] <build number> <version>"
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

if [[ $# -ne 2 ]]
then
    usage
    exit 1
fi

buildNumber=$1
version=$2

if [[ ! -z "$user" ]]
then
    user="${user}@"
fi

svnBase="svn+ssh://${user}www.cinnamonbob.com/usr/local/svn-repo/pulse"
svnTag="$svnBase/tags/$version"

#if svn info "$svnTag" > /dev/null 2>& 1
#then
#    yesNo "Tag $version already exists.  Use existing [yN]? "
#else
    svn copy -m "Tagging release $version" "$svnBase/branches/1.0.x" "$svnTag"
#fi

tmpDir=checkout.$$

yesNo "Build release now [yN]? "

svn co $svnTag $tmpDir
pushd $tmpDir
./build-release.sh $buildNumber $version

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
