@echo off
SET "JAVA_HOME=C:\Program Files\Java\jdk-17.0.1"
SET PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
java -version
echo.
echo Stopping Gradle daemon...
call gradlew.bat --stop
echo.
echo Building project...
call gradlew.bat clean assembleDebug
