@echo off
REM Download HikariCP Connection Pool Library
echo ========================================
echo Downloading HikariCP Library...
echo ========================================

if not exist "lib" mkdir lib

REM Download HikariCP
echo Downloading HikariCP-5.1.0.jar...
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar', 'lib\HikariCP-5.1.0.jar')"

if exist "lib\HikariCP-5.1.0.jar" (
    echo.
    echo ✓ HikariCP downloaded successfully!
    echo   Location: lib\HikariCP-5.1.0.jar
) else (
    echo.
    echo ✗ Failed to download HikariCP
    echo   Please download manually from:
    echo   https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar
    echo.
)

echo ========================================
pause
