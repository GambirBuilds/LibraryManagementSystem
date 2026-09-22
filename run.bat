@echo off
echo ========================================
echo   Library Management System - Windows
echo ========================================
echo.

if not exist "lib\sqlite-jdbc-3.36.0.3.jar" (
    echo [ERROR] SQLite JDBC driver not found in lib folder!
    pause
    exit /b 1
)

echo Creating bin folder...
if not exist bin mkdir bin

echo Compiling Java sources...
dir /s /b src\main\java\*.java > sources.txt 2>nul
javac -encoding UTF-8 -cp "lib\*" -d bin @sources.txt
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed!
    del sources.txt 2>nul
    pause
    exit /b 1
)
del sources.txt 2>nul

echo Compilation successful.
echo Starting application...
echo.
java -cp "bin;lib\*" com.library.Main

echo.
pause
