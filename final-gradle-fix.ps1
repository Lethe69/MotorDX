Write-Host "Fixing all Gradle configuration issues..." -ForegroundColor Green

# Step 1: Download the Gradle binary distribution if needed
$gradleVersion = "8.2"
$gradleZipFile = "C:\temp\gradle-$gradleVersion-bin.zip"
$gradleExtractDir = "C:\temp"
$gradleHomeDir = "$gradleExtractDir\gradle-$gradleVersion"

# Check if Gradle is already extracted
if (-not (Test-Path "$gradleHomeDir\bin\gradle.bat")) {
    Write-Host "Downloading Gradle $gradleVersion..." -ForegroundColor Yellow
    
    # Create the temp directory if it doesn't exist
    if (-not (Test-Path "C:\temp")) {
        New-Item -ItemType Directory -Path "C:\temp" -Force | Out-Null
    }
    
    # Download Gradle if needed
    if (-not (Test-Path $gradleZipFile)) {
        $downloadUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
        Invoke-WebRequest -Uri $downloadUrl -OutFile $gradleZipFile
    }
    
    # Extract Gradle
    Write-Host "Extracting Gradle..." -ForegroundColor Yellow
    Expand-Archive -Path $gradleZipFile -DestinationPath $gradleExtractDir -Force
}

# Step 2: Clean up any existing Gradle wrapper directories
Write-Host "Cleaning up Gradle wrapper directories..." -ForegroundColor Yellow
$gradleUserHome = "$env:USERPROFILE\.gradle"
$wrapperDir = "$gradleUserHome\wrapper"

# Ensure the main directories exist
New-Item -ItemType Directory -Path $gradleUserHome -Force | Out-Null
New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
New-Item -ItemType Directory -Path "$wrapperDir\dists" -Force | Out-Null

# Step 3: Update the Gradle wrapper properties
$wrapperPropsFile = "gradle\wrapper\gradle-wrapper.properties"
if (Test-Path $wrapperPropsFile) {
    Write-Host "Updating Gradle wrapper properties..." -ForegroundColor Yellow
    $wrapperProps = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
    Set-Content -Path $wrapperPropsFile -Value $wrapperProps
}

# Step 4: Update the gradlew.bat file
$gradlewBatFile = "gradlew.bat"
if (Test-Path $gradlewBatFile) {
    Write-Host "Updating gradlew.bat file..." -ForegroundColor Yellow
    $gradlewBatContent = Get-Content $gradlewBatFile -Raw
    $gradlewBatContent = $gradlewBatContent -replace "set GRADLE_HOME=.*", ""
    $gradlewBatContent = $gradlewBatContent -replace "set PATH=%GRADLE_HOME%\\bin;%PATH%", ""
    Set-Content -Path $gradlewBatFile -Value $gradlewBatContent
}

# Step 5: Update local.properties
$localPropsFile = "local.properties"
if (Test-Path $localPropsFile) {
    Write-Host "Updating local.properties file..." -ForegroundColor Yellow
    $localProps = Get-Content $localPropsFile -Raw
    
    # Ensure SDK path is correct
    if ($localProps -notmatch "sdk.dir=") {
        $localProps += "`nsdk.dir=C:\\Users\\$env:USERNAME\\AppData\\Local\\Android\\Sdk"
    }
    
    # Set Gradle dir
    if ($localProps -match "gradle.dir=") {
        $localProps = $localProps -replace "gradle.dir=.*", "gradle.dir=$($gradleHomeDir.Replace('\', '\\'))"
    } else {
        $localProps += "`ngradle.dir=$($gradleHomeDir.Replace('\', '\\'))"
    }
    
    Set-Content -Path $localPropsFile -Value $localProps
}

# Step 6: Update settings.gradle
$settingsGradleFile = "settings.gradle"
if (Test-Path $settingsGradleFile) {
    Write-Host "Updating settings.gradle file..." -ForegroundColor Yellow
    $settingsContent = @"
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "MobileDx"
include ':app'
"@
    Set-Content -Path $settingsGradleFile -Value $settingsContent
}

# Step 7: Update .idea/gradle.xml
$gradleXmlFile = ".idea\gradle.xml"
if (Test-Path $gradleXmlFile) {
    Write-Host "Updating .idea/gradle.xml file..." -ForegroundColor Yellow
    $gradleXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="GradleMigrationSettings" migrationVersion="1" />
  <component name="GradleSettings">
    <option name="linkedExternalProjectsSettings">
      <GradleProjectSettings>
        <option name="externalProjectPath" value="`$PROJECT_DIR`$" />
        <option name="gradleJvm" value="#JAVA_HOME" />
        <option name="distributionType" value="DEFAULT_WRAPPED" />
        <option name="modules">
          <set>
            <option value="`$PROJECT_DIR`$" />
            <option value="`$PROJECT_DIR`$/app" />
          </set>
        </option>
      </GradleProjectSettings>
    </option>
  </component>
</project>
"@
    Set-Content -Path $gradleXmlFile -Value $gradleXml
}

# Step 8: Try to build with our known-working Gradle installation
Write-Host "Trying to build with Gradle..." -ForegroundColor Green
& "$gradleHomeDir\bin\gradle.bat" assembleDebug

Write-Host "`nAll Gradle configuration has been fixed." -ForegroundColor Green
Write-Host "Try these steps in Android Studio:" -ForegroundColor Green
Write-Host "1. File > Settings > Build, Execution, Deployment > Gradle" -ForegroundColor Yellow
Write-Host "2. Set 'Use Gradle from' to 'Specified location'" -ForegroundColor Yellow
Write-Host "3. Set Gradle home to: $gradleHomeDir" -ForegroundColor Yellow
Write-Host "4. Click Apply and OK" -ForegroundColor Yellow
Write-Host "5. Restart Android Studio" -ForegroundColor Yellow 