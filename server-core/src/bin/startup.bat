@echo off

@setlocal

set DEFAULT_PULSE_HOME=%~dp0..
if "%PULSE_HOME%"=="" set PULSE_HOME=%DEFAULT_PULSE_HOME%

if exist "%PULSE_HOME%\bin\common.bat" goto havePulse

echo Could not find "%PULSE_HOME%\bin\common.bat", please
echo set PULSE_HOME
goto end

:havePulse

call "%PULSE_HOME%"\bin\common.bat start %*

:end
rem all done

@endlocal
