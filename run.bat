@echo off
setlocal
title Roberice POS & Inventory Management System (Java Desktop Application)

echo =====================================================================
echo   ROBERICE POS & INVENTORY MANAGEMENT SYSTEM - JAVA DESKTOP EDITION
echo =====================================================================
echo.

rem Detect or configure JAVA_HOME
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    )
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo [OK] Using Java runtime: %JAVA_HOME%
) else (
    where java >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Java JDK is not detected on your system.
        echo Please ensure JDK 17+ or JDK 21 is installed and JAVA_HOME is configured.
        pause
        exit /b 1
    )
)

echo [OK] Starting Roberice Desktop Application...
echo.

call "%~dp0mvnw.cmd" compile exec:java

if errorlevel 1 (
    echo.
    echo [ERROR] Application failed to start.
    pause
)
