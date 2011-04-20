#! /usr/bin/env bash

set -e

. "$(dirname $0)/hg-repo-common.inc"

if [[ -d "$repodir" ]]
then
    rm -rf "$repodir"
fi

mkdir -p "$repodir"

unzip -d "$repodir" "$dumpfile"

exit 0
