#! /usr/bin/env bash

set -e

. "$(dirname $0)/svn-repo-common.inc"

if [[ -f "$pidfile" ]]
then
    kill $(cat "$pidfile")
    rm "$pidfile"
fi

exit 0
