#! /usr/bin/env bash

set -e

. "$(dirname $0)/svn-repo-common.inc"

svnadmin dump "$repodir" | gzip -c > "$dumpfile"
exit 0
