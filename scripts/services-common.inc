. "$(dirname $0)/common.inc"

services="svn-repo git-repo p4-repo hg-repo apacheds"

if which Xvfb > /dev/null
then
    services="$services xvfb"
fi

