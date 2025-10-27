$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Using Java from: $env:JAVA_HOME"
java -version

Write-Host "`nBuilding project..."
& ".\gradlew.bat" "assembleDebug"
