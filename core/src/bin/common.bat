@echo off

@setlocal

set DEFAULT_PULSE_HOME=%~dp0..
if "%PULSE_HOME%"=="" set PULSE_HOME=%DEFAULT_PULSE_HOME%

if exist "%PULSE_HOME%\bin\common.bat" goto havePulse

echo Could not find "%PULSE_HOME%\bin\common.bat", please
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

set _EXECCMD="%_JAVACMD%"
if "%1" == "start" goto doStart
goto doExec

:doStart
set _EXECCMD=start "Pulse" "%_JAVACMD%"

:doExec

rem setup the classpath.
set LOCALCLASSPATH=%CLASSPATH%;"%PULSE_HOME%\system\www\WEB-INF\classes"
set LOCALCLASSPATH=%LOCALCLASSPATH%;"%PULSE_HOME%\lib"
for %%i in ("%PULSE_HOME%\lib\*.jar") do call "%PULSE_HOME%\bin\lcp.bat" %%i
for %%i in ("%PULSE_HOME%\lib\*.xml") do call "%PULSE_HOME%\bin\lcp.bat" %%i

%_EXECCMD% %PULSE_OPTS% -classpath "%LOCALCLASSPATH%" -Dpulse.home="%PULSE_HOME%" -Djava.util.logging.config.class=com.zutubi.pulse.logging.ConsoleConfig -Djava.awt.headless=true %*

rem if "%1" == "start" goto end
rem if errorlevel 1 pause
rem goto end

set LOCALCLASSPATH=

:end

@endlocal

