# This script builds the MobileDx app using our custom Gradle installation

Write-Host "Building MobileDx application..." -ForegroundColor Green

# Set Gradle home
$env:GRADLE_HOME = "C:\temp\gradle-8.2"
$env:PATH = "$env:GRADLE_HOME\bin;$env:PATH"

# Verify Gradle installation
if (-not (Test-Path "$env:GRADLE_HOME\bin\gradle.bat")) {
    Write-Host "ERROR: Gradle not found at $env:GRADLE_HOME\bin\gradle.bat" -ForegroundColor Red
    exit 1
}

# Clean build directory to avoid permission issues
if (Test-Path "app\build") {
    Write-Host "Cleaning previous build..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force "app\build" -ErrorAction SilentlyContinue
}

# Create build directory with proper permissions
New-Item -ItemType Directory -Path "app\build" -Force | Out-Null

# Check Android SDK path
$localPropsFile = "local.properties"
if (Test-Path $localPropsFile) {
    $sdkPath = Select-String -Path $localPropsFile -Pattern "sdk.dir" | ForEach-Object { $_.Line.Split('=')[1] }
    $sdkPath = $sdkPath -replace "\\\\", "\"
    
    Write-Host "Using Android SDK at: $sdkPath" -ForegroundColor Yellow
    if (-not (Test-Path $sdkPath)) {
        Write-Host "WARNING: Android SDK path not found at $sdkPath" -ForegroundColor Yellow
    }
} else {
    Write-Host "WARNING: local.properties file not found" -ForegroundColor Yellow
}

# Run the build
Write-Host "Running Gradle build..." -ForegroundColor Green
& "$env:GRADLE_HOME\bin\gradle.bat" assembleDebug --info

# Check if APK was built
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "Build successful! APK available at: $apkPath" -ForegroundColor Green
    
    # Get the APK file info
    $apkFile = Get-Item $apkPath
    Write-Host "APK Size: $([Math]::Round($apkFile.Length / 1MB, 2)) MB" -ForegroundColor Green
    Write-Host "Built on: $($apkFile.LastWriteTime)" -ForegroundColor Green
} else {
    Write-Host "Build may have failed. Check the logs above for errors." -ForegroundColor Red
    $buildDir = "app\build"
    if (Test-Path $buildDir) {
        $outputFiles = Get-ChildItem -Path $buildDir -Recurse -File -Include "*.apk" | Select-Object FullName
        if ($outputFiles.Count -gt 0) {
            Write-Host "Found these APK files:" -ForegroundColor Yellow
            $outputFiles | ForEach-Object { Write-Host $_.FullName -ForegroundColor Yellow }
        }
    }
}

Write-Host "Build process completed." -ForegroundColor Green 