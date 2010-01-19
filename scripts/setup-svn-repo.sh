#! /usr/bin/env bash

set -e

. "$(dirname $0)/svn-repo-common.inc"

if [[ -d "$repodir" ]]
then
    rm -rf "$repodir"
fi

mkdir -p "$repodir"
svnadmin create "$repodir"

cat > "$repodir/conf/svnserve.conf" <<EOF
[general]
anon-access = read
auth-access = write
password-db = passwd
EOF

cat > "$repodir/conf/passwd" <<EOF
[users]
pulse = pulse
pulse1 = pulse1
EOF

gunzip -c "$dumpfile" | svnadmin load "$repodir"
svnserve -d -r "$repodir" --listen-port=3088 --listen-host=127.0.0.1 --foreground &
echo $! > "$pidfile"

exit 0
