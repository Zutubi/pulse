@echo off
set I=0
:loop
echo longline longline longline longline longline longline longline longline longline longline longline longline %I%
set /A I=%I% + 1
if %I% LSS 1000 goto loop
