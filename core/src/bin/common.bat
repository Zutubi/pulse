@echo off

@setlocal

set DEFAULT_PULSE_HOME=%~dp0..
if "%PULSE_HOME%"=="" set PULSE_HOME=%DEFAULT_PULSE_HOME%

if exist "%PULSE_HOME%\boot.jar" goto havePulse

echo Could not find "%PULSE_HOME%\boot.jar", please
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

if "%PULSE_OPTS%"=="" set PULSE_OPTS=-Xmx512m

%_EXECCMD% %JAVA_OPTS% %PULSE_OPTS% -classpath "%PULSE_HOME%\boot.jar" -Dpulse.home="%PULSE_HOME%" -Djava.awt.headless=true %*

rem if "%1" == "start" goto end
rem if errorlevel 1 pause
rem goto end

:end

@endlocal

