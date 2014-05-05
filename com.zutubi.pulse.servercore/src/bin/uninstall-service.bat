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

"%PULSE_HOME%\bin\%ARCH%\PulseService" delete

:end

@endlocal

