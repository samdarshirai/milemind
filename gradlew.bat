@echo off
setlocal
set SCRIPT_DIR=%~dp0
call "%SCRIPT_DIR%apps\android\gradlew.bat" -p "%SCRIPT_DIR%apps\android" %*
