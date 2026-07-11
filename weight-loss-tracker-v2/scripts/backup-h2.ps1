[CmdletBinding()]
param(
    [string]$BackupRoot
)

$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $true

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$composeFile = Join-Path $repoRoot "deploy\compose.yml"
$dataDir = Join-Path $repoRoot "runtime-data\h2"
if (-not $BackupRoot) {
    $BackupRoot = Join-Path $repoRoot "backups\h2"
}

New-Item -ItemType Directory -Force -Path $dataDir, $BackupRoot | Out-Null
$backupDir = Join-Path $BackupRoot (Get-Date -Format "yyyyMMdd-HHmmss")
$runningServices = @(docker compose -f $composeFile ps --status running --services)
$wasRunning = $runningServices -contains "weight-loss-tracker"

if ($wasRunning) {
    docker compose -f $composeFile stop weight-loss-tracker
}

try {
    New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
    Get-ChildItem -Force -LiteralPath $dataDir | Copy-Item -Destination $backupDir -Recurse -Force
    Write-Host "H2 backup created: $backupDir"
}
finally {
    if ($wasRunning) {
        docker compose -f $composeFile start weight-loss-tracker
    }
}
