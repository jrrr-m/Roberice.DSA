@echo off
setlocal
title Build Roberice POS

where javac >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set JAVAC_CMD=javac
) else (
    if exist "C:\Users\User\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260801-1229\jre\bin\javac.exe" (
        set JAVAC_CMD="C:\Users\User\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260801-1229\jre\bin\javac.exe"
    ) else (
        echo [ERROR] javac compiler could not be located on your system.
        pause
        exit /b 1
    )
)

if not exist "bin" mkdir bin

echo Compiling Java source files...
powershell -Command "$sources = Get-ChildItem -Path 'src' -Filter '*.java' -Recurse | ForEach-Object { $_.FullName }; & %JAVAC_CMD% -cp 'lib\*' -d 'bin' $sources"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed without errors.
) else (
    echo [ERROR] Compilation failed.
)
pause
endlocal
