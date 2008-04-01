#! /usr/bin/env bash

set -e

echo "Waiting for pulse server to start..."
# Wait for pulse to come online
i=0
while ! netstat -an | grep '\b8889\b' > /dev/null
do
    sleep 2
    if [[ $i -eq 8 ]]
    then
        echo 'sheesh, I havent got all day!'
    elif [[ $i -eq 150 ]]
    then
        echo 'giving up'
        exit 1
    else
        echo "still waiting..."
    fi
    i=$((i + 1))
done

# Take a breath...
sleep 5
echo 'Ready!'

exit 0
