$ErrorActionPreference = 'Stop'
$entregaRoot = Split-Path $PSScriptRoot -Parent
$localValues = @{}
$envFile = Join-Path $entregaRoot '.env'
if (Test-Path -LiteralPath $envFile) {
    foreach ($line in [System.IO.File]::ReadAllLines($envFile)) {
        if ($line -match '^\s*([A-Z_][A-Z_0-9]*)\s*=(.*)$') {
            $localValues[$Matches[1]] = $Matches[2].Trim().Trim('"').Trim("'")
        }
    }
}
if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = $localValues['POSTGRES_PASSWORD'] }
if (-not $env:DB_PASSWORD) { throw 'Configure DB_PASSWORD no processo ou POSTGRES_PASSWORD no .env local.' }
if (-not $env:DB_USER) { $env:DB_USER = if ($localValues['POSTGRES_USER']) { $localValues['POSTGRES_USER'] } else { 'local_services' } }
if (-not $env:DB_URL) {
    $dbPort = if ($localValues['POSTGRES_PORT']) { $localValues['POSTGRES_PORT'] } else { '5433' }
    $dbName = if ($localValues['POSTGRES_DB']) { $localValues['POSTGRES_DB'] } else { 'local_services' }
    $env:DB_URL = "jdbc:postgresql://localhost:${dbPort}/$dbName"
}
if (-not $env:JWT_SECRET) {
    if ($localValues['JWT_SECRET']) { $env:JWT_SECRET = $localValues['JWT_SECRET'] }
    else {
        $secretFile = Join-Path $entregaRoot '.jwt-secret.local'
        if (-not (Test-Path -LiteralPath $secretFile)) {
            $jwtBytes = New-Object byte[] 32
            $jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
            try { $jwtRng.GetBytes($jwtBytes) } finally { $jwtRng.Dispose() }
            [System.IO.File]::WriteAllText($secretFile, [Convert]::ToBase64String($jwtBytes))
        }
        $env:JWT_SECRET = [System.IO.File]::ReadAllText($secretFile).Trim()
    }
}
