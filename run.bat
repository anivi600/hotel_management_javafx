@echo off
REM ============================================================
REM  Four Square Hotel Manipal Management System — Run Script
REM  Usage: Double-click this file or run from command prompt
REM ============================================================

SET JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
SET MAVEN_HOME=%~dp0maven\apache-maven-3.9.6
SET PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo.
echo  ========================================
echo    Four Square Hotel Manipal
echo  ========================================
echo   Java:  %JAVA_HOME%
echo   Maven: %MAVEN_HOME%
echo  ========================================
echo.

cd /d "%~dp0"
"%MAVEN_HOME%\bin\mvn.cmd" javafx:run

pause
