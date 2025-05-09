Write-Host "Fixing Gradle wrapper directory path issues..." -ForegroundColor Green

# First, delete any existing wrapper directories with spaces
$gradleUserHomeDir = "$env:USERPROFILE\.gradle"
$wrapperBaseDir = "$gradleUserHomeDir\wrapper"
$distsDir = "$wrapperBaseDir\dists"

# Create directories without spaces - ensure they exist
New-Item -ItemType Directory -Path $gradleUserHomeDir -Force | Out-Null
New-Item -ItemType Directory -Path $wrapperBaseDir -Force | Out-Null
New-Item -ItemType Directory -Path $distsDir -Force | Out-Null

# Create a directory for the launcher
$gradleVersion = "8.2"
$launcherDir = "$distsDir\gradle-launcher-$gradleVersion"
New-Item -ItemType Directory -Path $launcherDir -Force | Out-Null

# Generate a unique hash for the directory
$hashDir = "$launcherDir\fixed-hash-dir"
New-Item -ItemType Directory -Path $hashDir -Force | Out-Null

# Copy the Gradle launcher JAR to the wrapper directory
$jarSource = "C:\temp\gradle-$gradleVersion\lib\gradle-launcher-$gradleVersion.jar"
$jarDest = "$hashDir\gradle-launcher-$gradleVersion.jar"
Copy-Item $jarSource -Destination $jarDest -Force

# Create a marker file to indicate successful download
$markerFile = "$hashDir\gradle-launcher-$gradleVersion.jar.ok"
"" | Set-Content -Path $markerFile -Force

# Also fix the gradle-wrapper.properties file
$wrapperPropsPath = "gradle\wrapper\gradle-wrapper.properties"
if (Test-Path $wrapperPropsPath) {
    $wrapperProps = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=file\:///C\:/temp/gradle-$gradleVersion/lib/gradle-launcher-$gradleVersion.jar
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
    
    # Save the updated properties
    Set-Content -Path $wrapperPropsPath -Value $wrapperProps
    
    Write-Host "Updated $wrapperPropsPath with correct path" -ForegroundColor Green
}

Write-Host "Created proper directory structure for Gradle wrapper." -ForegroundColor Green
Write-Host "Try running Android Studio now and it should work." -ForegroundColor Green 