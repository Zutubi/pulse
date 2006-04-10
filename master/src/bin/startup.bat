@echo off

@setlocal

set DEFAULT_BOB_HOME=%~dp0..
if "%BOB_HOME%"=="" set BOB_HOME=%DEFAULT_BOB_HOME%

if exist "%BOB_HOME%\bin\common.bat" goto haveBob

echo Could not find "%BOB_HOME%\bin\common.bat", please
echo set BOB_HOME
goto end

:haveBob

call "%BOB_HOME%"\bin\common.bat com.zutubi.pulse.command.Bootstrap start %*

:end
rem all done

@endlocal

