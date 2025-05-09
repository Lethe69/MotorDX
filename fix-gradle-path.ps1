Write-Host "Fixing Gradle path references in project files..." -ForegroundColor Green

# Check if our Gradle installation exists
$gradlePath = "C:\temp\gradle-8.2"
if (-not (Test-Path $gradlePath)) {
    Write-Host "Error: Gradle installation not found at $gradlePath" -ForegroundColor Red
    Write-Host "Please make sure Gradle is installed in the correct location." -ForegroundColor Red
    exit 1
}

# Fix .idea/gradle.xml
$gradleXmlPath = ".\.idea\gradle.xml"
if (Test-Path $gradleXmlPath) {
    Write-Host "Updating $gradleXmlPath..." -ForegroundColor Yellow
    $content = Get-Content $gradleXmlPath -Raw
    
    # Replace references to double gradle-8.2 directory
    $content = $content -replace "gradle-8.2\\gradle-8.2", "gradle-8.2"
    $content = $content -replace "C:\\temp\\gradle-8\.2\\gradle-8\.2", "C:\\temp\\gradle-8.2"
    
    # Update distribution type and URL
    $content = $content -replace '<option name="distributionType" value=".*" />', '<option name="distributionType" value="LOCAL" />'
    if ($content -notmatch '<option name="distributionUrl"') {
        $content = $content -replace '<option name="gradleJvm"', '<option name="distributionUrl" value="file:///C:/temp/gradle-8.2" /><option name="gradleJvm"'
    } else {
        $content = $content -replace '<option name="distributionUrl" value=".*" />', '<option name="distributionUrl" value="file:///C:/temp/gradle-8.2" />'
    }
    
    Set-Content -Path $gradleXmlPath -Value $content
    Write-Host "Updated $gradleXmlPath successfully" -ForegroundColor Green
}

# Fix local.properties
$localPropertiesPath = ".\local.properties"
if (Test-Path $localPropertiesPath) {
    Write-Host "Updating $localPropertiesPath..." -ForegroundColor Yellow
    $content = Get-Content $localPropertiesPath -Raw
    
    # Add or update gradle.dir property
    if ($content -match "gradle\.dir=") {
        $content = $content -replace "gradle\.dir=.*", "gradle.dir=C\:\\temp\\gradle-8.2"
    } else {
        $content = $content + "`ngradle.dir=C\:\\temp\\gradle-8.2"
    }
    
    Set-Content -Path $localPropertiesPath -Value $content
    Write-Host "Updated $localPropertiesPath successfully" -ForegroundColor Green
}

Write-Host "Gradle path references have been fixed. Please restart Android Studio." -ForegroundColor Green 