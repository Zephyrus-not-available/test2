@echo off
cd /d "%~dp0"
echo Building project...
call mvn clean package -DskipTests
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build completed successfully!
    echo JAR file location: target\KTUVotingapp-0.0.1-SNAPSHOT.jar
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%
)
pause

