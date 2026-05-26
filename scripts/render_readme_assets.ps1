$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$screenshots = Join-Path $repoRoot "screenshots"
$stdout = Join-Path $env:TEMP ("field-audit-mobile-" + [guid]::NewGuid().ToString() + "-stdout.log")
$stderr = Join-Path $env:TEMP ("field-audit-mobile-" + [guid]::NewGuid().ToString() + "-stderr.log")
$port = 5572
$process = $null
$edgeCandidates = @(
  "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
  "C:\Program Files\Microsoft\Edge\Application\msedge.exe"
)

New-Item -ItemType Directory -Force -Path $screenshots | Out-Null

function Get-EdgePath {
  foreach ($candidate in $edgeCandidates) {
    if (Test-Path $candidate) {
      return $candidate
    }
  }
  throw "Microsoft Edge was not found."
}

function Wait-ForUrl {
  param([string]$Url)
  for ($i = 0; $i -lt 60; $i++) {
    try {
      Invoke-WebRequest -Uri $Url -UseBasicParsing | Out-Null
      return
    } catch {
      Start-Sleep -Milliseconds 1000
    }
  }
  throw "Timed out waiting for $Url"
}

try {
  $process = Start-Process -FilePath ".\\gradlew.bat" `
    -ArgumentList "run", "--args=server" `
    -WorkingDirectory $repoRoot `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -WindowStyle Hidden `
    -PassThru

  Wait-ForUrl "http://127.0.0.1:$port/"

  $edge = Get-EdgePath
  $targets = @(
    @{ Url = "http://127.0.0.1:$port/"; File = "01-overview-proof.png"; Size = "440,1240" },
    @{ Url = "http://127.0.0.1:$port/audit-lane"; File = "02-audit-lane-proof.png"; Size = "440,1320" },
    @{ Url = "http://127.0.0.1:$port/capture-packets"; File = "03-capture-packets-proof.png"; Size = "440,1320" },
    @{ Url = "http://127.0.0.1:$port/verification"; File = "04-verification-proof.png"; Size = "440,1260" }
  )

  foreach ($target in $targets) {
    & $edge `
      --headless `
      --disable-gpu `
      --hide-scrollbars `
      "--window-size=$($target.Size)" `
      "--screenshot=$(Join-Path $screenshots $target.File)" `
      $target.Url | Out-Null
  }
} finally {
  if ($process -and -not $process.HasExited) {
    Stop-Process -Id $process.Id -Force
  }

  if (Test-Path $stdout) { Remove-Item $stdout -Force }
  if (Test-Path $stderr) { Remove-Item $stderr -Force }
}
