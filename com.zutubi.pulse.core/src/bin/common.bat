@echo off

@setlocal

set DEFAULT_PULSE_HOME=%~dp0..
if "%PULSE_HOME%"=="" set PULSE_HOME=%DEFAULT_PULSE_HOME%

set BOOT_JAR=%PULSE_HOME%\lib\boot.jar
if exist "%BOOT_JAR%" goto havePulse

echo Could not find "%BOOT_JAR%", please
echo set PULSE_HOME
goto end

:havePulse

set _JAVACMD=%JAVACMD%

if not defined JAVA_HOME goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if not defined _JAVACMD set _JAVACMD=%JAVA_HOME%\bin\java.exe

if exist "%_JAVACMD%" goto haveJava
echo Could not find "%_JAVACMD%", please set JAVA_HOME or JAVACMD,
echo or ensure java.exe is in the PATH.
goto end

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:haveJava

if "%JAVA_OPTS%"=="" set JAVA_OPTS="-Xmx1024m -XX:MaxPermSize=128m"

:restart

"%_JAVACMD%" %JAVA_OPTS% -classpath "%BOOT_JAR%" -Dpulse.home="%PULSE_HOME%" -Djava.awt.headless=true -Djava.util.logging.config.class=com.zutubi.pulse.logging.ConsoleConfig com.zutubi.pulse.command.PulseCtl %*

set CODE=%ERRORLEVEL%
if %CODE% equ 111 goto restart

:end

@endlocal

exit /B %CODE%
