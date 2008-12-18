#! /usr/bin/env bash

set -e

. "$(dirname $0)/p4-repo-common.inc"

if [[ -d "$repodir" ]]
then
    rm -rf "$repodir"
fi

mkdir -p "$repodir"
unzip -d "$repodir" "$archive"

p4d -r "$repodir" -jr checkpoint.1
p4d -r "$repodir" -p $port > "$working/p4d-stdout.txt" 2> "$working/p4d-stderr.txt" &
echo $! > "$pidfile"

exit 0
