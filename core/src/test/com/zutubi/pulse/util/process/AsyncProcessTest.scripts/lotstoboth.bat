@echo off
set J=0
:loop
call %~dp0lots.bat
call %~dp0lots.bat 1>&2
set /A J=%J% + 1
if %J% LSS 4 goto loop
