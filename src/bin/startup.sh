#! /bin/sh

cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# Determine bobs home directory if it has not been specified.
if [ -z "$BOB_HOME" -o ! -d "$BOB_HOME" ] ; then
  PRG="$0"
  BOB_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  BOB_HOME=`cd "$BOB_HOME" && pwd`
fi

if [ ! -f "$BOB_HOME/lib/bob.jar" ] ; then
  echo "Error: BOB_HOME is not defined correctly."
  echo "  We cannot find $BOB_HOME/lib/bob.jar"
  exit 1
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$BOB_HOME" ] && BOB_HOME=`cygpath --path --unix "$BOB_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# Construct the runtime classpath.
for i in "$BOB_HOME"/lib/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done

# For Cygwin, switch paths to Windows format before running java                                  
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  BOB_HOME=`cygpath --path --windows "$BOB_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
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

exec "$JAVACMD" -classpath "$CLASSPATH" -Dbob.home="$BOB_HOME" -Djava.awt.headless=true com.cinnamonbob.command.Bootstrap --start