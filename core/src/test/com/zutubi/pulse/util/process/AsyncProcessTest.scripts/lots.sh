#! /bin/bash

i=0
while [[ $i -lt 1000 ]]
do
    echo longline longline longline longline longline longline longline longline longline longline longline longline $i
    i=$((i + 1))
done
exit 0

