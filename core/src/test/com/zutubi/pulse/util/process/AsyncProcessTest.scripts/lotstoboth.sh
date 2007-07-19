#! /bin/bash
lots=$(dirname $0)/lots.sh
for i in $(seq 0 3)
do
    bash $lots
    bash $lots 1>&2
done
exit 0
