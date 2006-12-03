#! /usr/bin/env bash

#set -e

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

        if wget "http://confluence.zutubi.com/display/pulse0102/$tag" -o /dev/null
        then
            ok=$((ok + 1))
            status="$tag: ok"
        else
            failed=$((failed + 1))
            status="$tag: FAILED"
            failures="$failures\n$filename"
        fi
    else
        notag=$((notag + 1))
        status="No help tag"
    fi

    echo "${filename#master/src/www/}: $status"
done

echo
echo "Total: $count, ok: $ok, FAILED: $failed, No Tag: $notag"

if [[ $failed -gt 0 ]]
then
    echo
    echo "======== ALL FAILURES ========"
    echo -e $failures
fi

exit 0
