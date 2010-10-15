#! /bin/sh

cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# Determine pulses home directory if it has not been specified.
if [ -z "$PULSE_HOME" -o ! -d "$PULSE_HOME" ] ; then
  PRG="$0"
  PULSE_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  PULSE_HOME=`cd "$PULSE_HOME" && pwd`
fi


# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$PULSE_HOME" ] && PULSE_HOME=`cygpath --path --unix "$PULSE_HOME"`
fi

BOOT_JAR="$PULSE_HOME/lib/boot.jar"

if [ ! -f "$BOOT_JAR" ] ; then
  echo "Error: PULSE_HOME is not defined correctly."
  echo "  Unable to find boot.jar"
  exit 1
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  PULSE_HOME=`cygpath --path --windows "$PULSE_HOME"`
  BOOT_JAR=`cygpath --path --windows "$BOOT_JAR"`
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

if [ -z "$JAVA_OPTS" ]
then
    JAVA_OPTS=-Xmx512m
fi

code=111
while [ $code -eq 111 ]
do
    if [ -z "$PULSE_OUT" ]
    then
        "$JAVACMD" $JAVA_OPTS -classpath "$BOOT_JAR" -Dpulse.home="$PULSE_HOME" -Djava.awt.headless=true -Djava.util.logging.config.class=com.zutubi.pulse.logging.ConsoleConfig com.zutubi.pulse.command.PulseCtl "$@"
    else
        "$JAVACMD" $JAVA_OPTS -classpath "$BOOT_JAR" -Dpulse.home="$PULSE_HOME" -Djava.awt.headless=true -Djava.util.logging.config.class=com.zutubi.pulse.logging.ConsoleConfig com.zutubi.pulse.command.PulseCtl "$@" >> "$PULSE_OUT" 2>&1
    fi
    code=$?
done

exit $code
