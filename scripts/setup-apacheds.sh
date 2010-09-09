#! /usr/bin/env bash

set -e

. "$(dirname $0)/apacheds-common.inc"

if netstat -an | grep \\b$port\\b > /dev/null
then
    echo Can not start apacheds, port $port is in use.
    exit 1
fi

# Prepare a temp directory to test within
unpackDir="$working/apacheds"
rm -rf "$unpackDir"
mkdir -p "$unpackDir"

# unpack apacheds into working directory
unpack "$top/com.zutubi.pulse.acceptance/src/test/misc/apacheds-1.5.7.tar.gz" "$unpackDir"

# update the server.xml configuration file with necessary paths.
sed -i "s#\$UNPACK_DIR#$unpackDir#g" $unpackDir/conf/server.xml

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

CP_SEP=':'
if $cygwin; then
  CP_SEP=';'
fi

#
# The following is taken directly from the apacheds.sh
# start: apacheds.sh
ADS_CP=
for i in `ls $unpackDir/lib/`
do
  _LIB="$unpackDir/lib/${i}"
  if $cygwin; then
    # Munge the path appropriately if we are running via cygwin.
    _LIB=`cygpath --windows "$unpackDir/lib/${i}"`
  fi
  ADS_CP="${ADS_CP}${CP_SEP}${_LIB}"
done

java -Dlog4j.configuration=file:$unpackDir/conf/log4j.properties -Dapacheds.log.dir=$unpackDir/logs -cp $ADS_CP org.apache.directory.server.UberjarMain $unpackDir/conf/server.xml > "$working/apacheds-stdout.txt" 2> "$working/apacheds-stderr.txt" &

#
# stop: apacheds.sh
#

echo $! > "$pidfile"


exit 0

