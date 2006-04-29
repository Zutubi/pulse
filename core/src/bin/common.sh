#! /bin/sh

cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# Determine pulses data directory if it has not been specified.
if [ -z "$PULSE_HOME" -o ! -d "$PULSE_HOME" ] ; then
  PRG="$0"
  PULSE_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  PULSE_HOME=`cd "$PULSE_HOME" && pwd`
fi

if [ ! -f "$PULSE_HOME"/lib/core-[0-9]*.jar ] ; then
  echo "Error: PULSE_HOME is not defined correctly."
  echo "  We cannot find the core jar"
  exit 1
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$PULSE_HOME" ] && PULSE_HOME=`cygpath --path --unix "$PULSE_HOME"`
  [ -n "$LOCALCLASSPATH" ] && LOCALCLASSPATH=`cygpath --path --unix "$LOCALCLASSPATH"`
fi

# Construct the runtime classpath.
for i in "$PULSE_HOME"/system/www/WEB-INF/classes \
         "$PULSE_HOME"/lib                        \
         "$PULSE_HOME"/lib/*.jar                  \
         "$PULSE_HOME"/lib/*.xml; do
  LOCALCLASSPATH="$LOCALCLASSPATH":"$i"
done

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  PULSE_HOME=`cygpath --path --windows "$PULSE_HOME"`
  LOCALCLASSPATH=`cygpath --path --windows "$LOCALCLASSPATH"`
fi

# Sort out the location of the java executable.
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$PULSE_PID" ]
then
    exec "$JAVACMD" -classpath "$LOCALCLASSPATH" -Dpulse.home="$PULSE_HOME" -Djava.awt.headless=true $@
else
    "$JAVACMD" -classpath "$LOCALCLASSPATH" -Dpulse.home="$PULSE_HOME" -Djava.awt.headless=true $@ >> "$PULSE_HOME"/pulse.out 2>&1 &
    echo $! > $PULSE_PID
    exit 0
fi
