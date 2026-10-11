@REM ----------------------------------------------------------------------------
@REM MiniJava Optimizer Maven Wrapper Script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

if exist "C:\Users\ADMIN\.maven\apache-maven-3.9.16\bin\mvn.cmd" (
    "C:\Users\ADMIN\.maven\apache-maven-3.9.16\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

echo Error: Maven executable not found. Please install Maven or add it to PATH.
exit /b 1
