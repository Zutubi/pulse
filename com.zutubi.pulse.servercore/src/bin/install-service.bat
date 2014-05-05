@echo off

@setlocal

set DEFAULT_PULSE_HOME=%~dp0..
if "%PULSE_HOME%"=="" set PULSE_HOME=%DEFAULT_PULSE_HOME%

if exist "%PULSE_HOME%\bin\common.bat" goto havePulse

echo Could not find "%PULSE_HOME%\bin\common.bat", please
echo set PULSE_HOME
goto end

:havePulse

if defined PROGRAMFILES(X86) (set ARCH=amd64) else (set ARCH=x86)

"%PULSE_HOME%\bin\%ARCH%\PulseService" install ^
  --Classpath="%PULSE_HOME%\lib\boot.jar" ^
  --JvmMx=1024 ++JvmOptions=-XX:MaxPermSize=128m ^
  ++JvmOptions=-Dpulse.home="%PULSE_HOME%" ++JvmOptions=-Djava.awt.headless=true ^
  ++JvmOptions=-Djava.util.logging.config.class=com.zutubi.pulse.logging.ConsoleConfig ^
  --StartClass=com.zutubi.pulse.command.PulseCtl --StartParams=start ^
  --StopClass=com.zutubi.pulse.command.PulseCtl --StopParams=stopservice

:end

@endlocal

