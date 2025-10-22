@echo off
SET JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot
SET PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
java -version
echo.
echo Stopping Gradle daemon...
call gradlew.bat --stop
echo.
echo Building project...
call gradlew.bat clean assembleDebug
