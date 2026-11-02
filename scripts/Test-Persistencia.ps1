$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/Import-LocalEnvironment.ps1"
$jar = (Resolve-Path (Join-Path $entregaRoot 'backend/target/local-services-api-0.0.1-SNAPSHOT.jar')).Path
$baseUrl = 'http://localhost:18080'
$persistEmail = 'persistencia-' + [guid]::NewGuid().ToString('N') + '@example.com'
$persistPassword = 'SenhaFicticiaPersistencia123!'
$script:persistApi = $null
$registro = $null
$dbUser = if ($localValues['POSTGRES_USER']) { $localValues['POSTGRES_USER'] } else { 'local_services' }
$dbName = if ($localValues['POSTGRES_DB']) { $localValues['POSTGRES_DB'] } else { 'local_services' }

function Iniciar-ApiPersistencia([string]$etapa) {
    $script:persistApi = Start-Process -FilePath (Get-Command java).Source -WindowStyle Hidden -PassThru `
        -ArgumentList @('-jar', ('"' + $jar + '"'), '--server.port=18080', '--spring.profiles.active=demo') `
        -WorkingDirectory $entregaRoot `
        -RedirectStandardOutput (Join-Path $entregaRoot "backend/target/persistencia-$etapa.log") `
        -RedirectStandardError (Join-Path $entregaRoot "backend/target/persistencia-$etapa.err")
    for ($tentativa = 0; $tentativa -lt 120; $tentativa++) {
        if ($script:persistApi.HasExited) { throw "A API temporária encerrou; consulte persistencia-$etapa.log." }
        try { $health = Invoke-RestMethod "$baseUrl/api/v1/health"; if ($health.status -eq 'UP') { return } } catch { }
        Start-Sleep -Milliseconds 500
    }
    throw 'A API temporária não iniciou no prazo.'
}
function Parar-ApiPersistencia {
    if ($script:persistApi -and -not $script:persistApi.HasExited) {
        $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId=$($script:persistApi.Id)"
        if (-not $processInfo -or -not $processInfo.CommandLine.Contains($jar) -or -not $processInfo.CommandLine.Contains('--server.port=18080')) {
            throw 'A identidade do processo temporário mudou; encerramento cancelado.'
        }
        Stop-Process -Id $script:persistApi.Id -Force
        $script:persistApi.WaitForExit()
    }
}
function Token-Csrf($sessao) { return (Invoke-RestMethod "$baseUrl/api/v1/auth/csrf" -WebSession $sessao).token }
function Entrar-Persistencia($sessao) {
    $token = Token-Csrf $sessao
    return Invoke-RestMethod "$baseUrl/api/v1/auth/login" -Method Post -WebSession $sessao `
        -Headers @{'X-CSRF-TOKEN'=$token} -ContentType 'application/json' `
        -Body (@{email=$persistEmail;senha=$persistPassword} | ConvertTo-Json)
}

Push-Location $entregaRoot
try {
    # A porta temporária deve estar livre; o IntelliJ em 8080 não é encerrado.
    if (Get-NetTCPConnection -State Listen -LocalPort 18080 -ErrorAction SilentlyContinue) { throw 'A porta 18080 está ocupada.' }
    $containerAntes = docker compose ps -q db
    if ($LASTEXITCODE -ne 0 -or -not $containerAntes) { throw 'O PostgreSQL precisa estar em execução.' }
    $mountsAntes = docker inspect --format '{{json .Mounts}}' $containerAntes | ConvertFrom-Json
    $volumeAntes = ($mountsAntes | Where-Object Destination -eq '/var/lib/postgresql/data').Name
    if (-not $volumeAntes) { throw 'Não foi possível identificar o volume persistente do PostgreSQL.' }
    Iniciar-ApiPersistencia 'antes'
    $sessao = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $token = Token-Csrf $sessao
    $registro = Invoke-RestMethod "$baseUrl/api/v1/auth/register" -Method Post -WebSession $sessao `
        -Headers @{'X-CSRF-TOKEN'=$token} -ContentType 'application/json' `
        -Body (@{nome='Teste Persistencia';email=$persistEmail;senha=$persistPassword} | ConvertTo-Json)
    $null = Entrar-Persistencia $sessao
    $token = Token-Csrf $sessao
    $perfil = Invoke-RestMethod "$baseUrl/api/v1/me/perfil-profissional" -Method Post -WebSession $sessao `
        -Headers @{'X-CSRF-TOKEN'=$token} -ContentType 'application/json' `
        -Body (@{nome='Perfil Persistencia';descricao='Perfil ficticio para verificar reinicio de processos.'} | ConvertTo-Json)
    Parar-ApiPersistencia

    docker compose up -d db | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar o container com a imagem fixada.' }
    docker compose restart db | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao reiniciar o PostgreSQL.' }
    $containerDepois = docker compose ps -q db
    for ($tentativa = 0; $tentativa -lt 90; $tentativa++) {
        $health = docker inspect --format '{{.State.Health.Status}}' $containerDepois
        if ($health -eq 'healthy') { break }
        Start-Sleep -Milliseconds 500
    }
    if ($health -ne 'healthy') { throw 'O PostgreSQL não ficou saudável no prazo.' }
    $mountsDepois = docker inspect --format '{{json .Mounts}}' $containerDepois | ConvertFrom-Json
    $volumeDepois = ($mountsDepois | Where-Object Destination -eq '/var/lib/postgresql/data').Name
    if (-not $volumeAntes -or $volumeAntes -ne $volumeDepois) { throw 'O volume do banco mudou inesperadamente.' }

    Iniciar-ApiPersistencia 'depois'
    $sessaoNova = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $identidade = Entrar-Persistencia $sessaoNova
    $perfilDepois = Invoke-RestMethod "$baseUrl/api/v1/me/perfil-profissional" -WebSession $sessaoNova
    if ($identidade.id -ne $registro.id -or $perfilDepois.id -ne $perfil.id -or $perfilDepois.agenda.id -ne $perfil.agenda.id) {
        throw 'Os identificadores persistidos não correspondem aos anteriores.'
    }
    @{data=(Get-Date).ToString('s');resultado='aprovado';usuarioId=$registro.id;perfilId=$perfil.id;agendaId=$perfil.agenda.id;volume=$volumeDepois;demo='verificado em duas inicializacoes'} |
        ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $entregaRoot 'backend/target/persistencia.json')
    Write-Output 'Persistência aprovada: conta, perfil e agenda preservados após reiniciar API e PostgreSQL; mesmo volume.'
} finally {
    if ($registro) {
        $safeId = [guid]::Parse($registro.id).ToString()
        $cleanup = "BEGIN; DELETE FROM agendas WHERE perfil_profissional_id IN (SELECT id FROM perfis_profissionais WHERE usuario_id = '$safeId'); DELETE FROM perfis_profissionais WHERE usuario_id = '$safeId'; DELETE FROM usuarios WHERE id = '$safeId' AND email = '$persistEmail'; COMMIT;"
        $cleanup | docker compose exec -T db psql -U $dbUser -d $dbName -v ON_ERROR_STOP=1 | Out-Null
        if ($LASTEXITCODE -ne 0) { Write-Warning 'Falha na limpeza da conta fictícia exclusiva do teste; consulte persistencia.json.' }
    }
    Parar-ApiPersistencia
    Pop-Location
}
