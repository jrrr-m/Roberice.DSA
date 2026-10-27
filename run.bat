@echo off
setlocal
title Roberice POS & Inventory Management System

:: Check if standard java is available, otherwise use Eclipse JustJ JDK
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set JAVA_CMD=java
) else (
    if exist "C:\Users\User\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260801-1229\jre\bin\java.exe" (
        set JAVA_CMD="C:\Users\User\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260801-1229\jre\bin\java.exe"
    ) else (
        echo [ERROR] Java could not be located on your system.
        echo Please ensure JDK 17+ or Eclipse is installed.
        pause
        exit /b 1
    )
)

echo Starting Roberice POS & Inventory Management System...
%JAVA_CMD% -cp "bin;lib\*" com.roberice.pos.ui.RobericeApp

endlocal
