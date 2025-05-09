@echo off
echo Building MotorDX APK with Gradle...

REM Set paths
set GRADLE_HOME=C:\temp\gradle-8.2
set PATH=%GRADLE_HOME%\bin;%PATH%

REM Change to project directory
cd %~dp0

REM Run Gradle build
%GRADLE_HOME%\bin\gradle assembleRelease

echo Build completed.
pause 