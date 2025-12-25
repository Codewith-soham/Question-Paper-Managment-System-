@echo off
setlocal enabledelayedexpansion

rem One-file starter: downloads libs, compiles sources, runs LaunchQPMS (auto DB + web server + browser)
set "BASE=%~dp0"
set "SRC=%BASE%src"
set "LIB=%BASE%lib"

pushd "%BASE%" >nul

echo ==================================================
echo Question Paper Management System - Start
echo ==================================================

echo [1/5] Checking Java...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found in PATH. Install JDK and retry.
    popd
    pause
    exit /b 1
)
echo Java OK.

echo [2/5] Ensuring lib folder exists...
if not exist "%LIB%" mkdir "%LIB%"

echo [3/5] Downloading required libraries if missing...
call :download "jakarta.mail-2.0.1.jar" "https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar"
call :download "jakarta.activation-2.0.1.jar" "https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar"
call :download "mysql-connector-j-8.0.33.jar" "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"

echo [4/5] Compiling Java sources...
pushd "%SRC%" >nul
javac -cp ".;%LIB%\*" *.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed. See errors above.
    popd
    popd
    pause
    exit /b 1
)
popd >nul
echo Compilation successful.

echo [5/5] Starting launcher (may open browser)...
java -cp "%SRC%;%LIB%\*" LaunchQPMS
set EXIT_CODE=%ERRORLEVEL%

popd >nul

if %EXIT_CODE% NEQ 0 (
    echo LaunchQPMS exited with code %EXIT_CODE%.
    pause
    exit /b %EXIT_CODE%
)

echo Launcher exited. Close this window to finish.
pause
exit /b 0

:download
rem %1 = file name, %2 = url
if exist "%LIB%\%~1" (
    echo     Found %~1
    goto :eof
)
echo     Downloading %~1 ...
powershell -Command "Invoke-WebRequest -Uri '%~2' -OutFile '%LIB%\%~1' -UseBasicParsing" 2>nul
if not exist "%LIB%\%~1" (
    echo     ! WARNING: Could not download %~1. Please fetch manually into lib.
)
goto :eof
