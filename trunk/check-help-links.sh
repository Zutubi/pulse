#! /usr/bin/env bash

set -e

tmpFile=$$.tmp
trap "rm -f $tmpFile" ERR

count=0
ok=0
failed=0
notag=0
failures=

for filename in $(find master/src/www -name '*.vm')
do
    count=$((count + 1))

    if grep helpTag $filename > /dev/null
    then
        tag=$(grep helpTag $filename)
        tag=${tag#*\"}
        tag=${tag%\"*}

        wget -O $tmpFile "http://confluence.zutubi.com/display/pulse0101/$tag" -o /dev/null
        if grep "Page Not Found" $tmpFile > /dev/null
        then
            failed=$((failed + 1))
            status="$tag: FAILED"
            failures="$failures\n$filename"
        else
            ok=$((ok + 1))
            status="$tag: ok"
        fi
    else
        notag=$((notag + 1))
        status="No help tag"
    fi

    echo "${filename#master/src/www/}: $status"
done

rm -f $tmpFile
trap ERR

echo
echo "Total: $count, ok: $ok, FAILED: $failed, No Tag: $notag"

if [[ $failed -gt 0 ]]
then
    echo
    echo "======== ALL FAILURES ========"
    echo -e $failures
fi

exit 0
