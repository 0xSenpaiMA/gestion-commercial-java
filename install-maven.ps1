# Install Maven on Windows
Write-Host "Installing Apache Maven..." -ForegroundColor Green

# Download Maven
$mavenVersion = "3.9.6"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$downloadPath = "$env:TEMP\apache-maven-$mavenVersion-bin.zip"
$extractPath = "C:\maven"

Write-Host "Downloading Maven from $mavenUrl..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $mavenUrl -OutFile $downloadPath -UseBasicParsing
    Write-Host "✓ Maven downloaded successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to download Maven: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Extract Maven
Write-Host "Extracting Maven to $extractPath..." -ForegroundColor Yellow
try {
    if (Test-Path $extractPath) {
        Remove-Item $extractPath -Recurse -Force
    }
    
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($downloadPath, "C:\")
    
    # Rename the extracted folder
    $extractedFolder = "C:\apache-maven-$mavenVersion"
    if (Test-Path $extractedFolder) {
        Rename-Item $extractedFolder $extractPath
        Write-Host "✓ Maven extracted successfully" -ForegroundColor Green
    } else {
        throw "Extracted folder not found"
    }
} catch {
    Write-Host "✗ Failed to extract Maven: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Set environment variables
Write-Host "Setting environment variables..." -ForegroundColor Yellow
try {
    # Set M2_HOME
    [Environment]::SetEnvironmentVariable("M2_HOME", $extractPath, [EnvironmentVariableTarget]::Machine)
    [Environment]::SetEnvironmentVariable("MAVEN_HOME", $extractPath, [EnvironmentVariableTarget]::Machine)
    
    # Update PATH
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", [EnvironmentVariableTarget]::Machine)
    $mavenBinPath = "$extractPath\bin"
    
    if ($currentPath -notlike "*$mavenBinPath*") {
        $newPath = "$currentPath;$mavenBinPath"
        [Environment]::SetEnvironmentVariable("PATH", $newPath, [EnvironmentVariableTarget]::Machine)
    }
    
    # Update current session PATH
    $env:M2_HOME = $extractPath
    $env:MAVEN_HOME = $extractPath
    $env:PATH = "$env:PATH;$mavenBinPath"
    
    Write-Host "✓ Environment variables set successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to set environment variables: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Verify installation
Write-Host "Verifying Maven installation..." -ForegroundColor Yellow
try {
    $mavenVersion = & "$extractPath\bin\mvn.cmd" -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Maven installed successfully!" -ForegroundColor Green
        Write-Host $mavenVersion -ForegroundColor Cyan
    } else {
        throw "Maven verification failed"
    }
} catch {
    Write-Host "✗ Maven verification failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Clean up
Remove-Item $downloadPath -Force

Write-Host "`n✓ Maven installation completed!" -ForegroundColor Green
Write-Host "Please restart your command prompt or PowerShell to use Maven globally." -ForegroundColor Yellow
Write-Host "Or use the full path: $extractPath\bin\mvn.cmd" -ForegroundColor Cyan
