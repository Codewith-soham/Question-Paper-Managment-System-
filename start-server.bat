@echo off
echo.
echo ========================================
echo Question Paper Management System v2.1
echo Production-Ready Edition
echo ========================================
echo.

REM Check if config.properties exists
if not exist "config.properties" (
    echo ⚠ Warning: config.properties not found
    echo Creating default configuration...
    echo.
)

REM Check if target directory exists
if not exist "target\classes" (
    echo Creating target directory...
    mkdir target\classes
    echo.
)

REM Check if lib directory exists
if not exist "lib" (
    echo ERROR: lib folder not found!
    echo Please ensure all required JAR files are in the lib folder.
    echo.
    echo Missing: lib folder with:
    echo   - mysql-connector-j-8.x.x.jar
    echo   - jackson-*.jar
    echo   - jakarta.mail-*.jar
    echo   - jakarta.activation-*.jar
    echo   - HikariCP-5.x.x.jar (can be downloaded with download-hikaricp.bat)
    echo.
    pause
    exit /b 1
)

REM Check if HikariCP is present
if not exist "lib\HikariCP*" (
    echo.
    echo ⚠ HikariCP library not found!
    echo Please run: download-hikaricp.bat
    echo.
    pause
    exit /b 1
)

REM Check if PDF directory exists
if not exist "PDF" (
    echo Creating PDF directory...
    mkdir PDF
    echo.
)

REM Compile Java files
echo Compiling Java source files...
javac -d target\classes -cp "lib\*" src\*.java
if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo Please check:
    echo   1. Java version (JDK 11+)
    echo   2. All source files in src\ folder
    echo   3. All JAR files in lib\ folder
    echo.
    pause
    exit /b 1
)

echo ✓ Compilation successful!
echo.

REM Start the server
echo ========================================
echo Starting Web Server...
echo ========================================
echo.
echo Access the application at:
echo   http://localhost:8080/frontend/index.html
echo.
echo Configuration: config.properties
echo Upload Directory: PDF/
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

java -cp "target\classes;lib\*" LaunchQPMS

pause
