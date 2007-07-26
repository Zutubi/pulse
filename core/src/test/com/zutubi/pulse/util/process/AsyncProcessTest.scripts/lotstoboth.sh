#! /bin/bash
lots=$(dirname $0)/lots.sh
for i in 0 1 2 3
do
    bash $lots
    bash $lots 1>&2
done
exit 0
