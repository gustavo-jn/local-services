param([ValidateSet('Run','Test','Package')][string]$Task = 'Run', [int]$Port = 8080, [switch]$Demo)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/Import-LocalEnvironment.ps1"
Push-Location (Join-Path $entregaRoot 'backend')
try {
    switch ($Task) {
        'Run' {
            $runArguments = "--server.port=$Port"
            if ($Demo) { $runArguments += ' --spring.profiles.active=demo' }
            & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=$runArguments"
        }
        'Test' { & .\mvnw.cmd test }
        'Package' { & .\mvnw.cmd package }
    }
    $backendExit = $LASTEXITCODE
} finally { Pop-Location }
exit $backendExit
