# Local Services

Plataforma web para divulgação, busca e contratação de serviços locais. O projeto aproxima clientes de profissionais autônomos e contempla, em sua evolução, perfis profissionais, solicitações, propostas, agenda, acompanhamento de atendimentos, comunicação e avaliações.

## Estado atual

Primeira entrega: Home, cadastro, login com JWT em cookie `HttpOnly`, identificação após recarregar, área privada, logout, alteração de nome/e-mail/senha e criação/edição do perfil profissional privado com agenda inicial. Backend Java 25/Spring Boot, frontend Angular e PostgreSQL 17.10 em Docker, com migrações Flyway.

O roteiro de apresentação está em [docs/roteiro-demo.md](docs/roteiro-demo.md); o escopo e as evidências estão em [docs/entrega-20.md](docs/entrega-20.md) e [docs/testes.md](docs/testes.md).

## Início rápido no Windows

Na raiz, copie `.env.example` para `.env` se ainda não existir e escolha a senha do PostgreSQL. Preserve o `.env` existente e a senha usada no volume já criado.

```powershell
docker compose up -d
docker compose ps
.\scripts\Invoke-Backend.ps1 -Task Run
```

Em outro terminal, dentro de `frontend`, com Node 26 selecionado:

```powershell
npm.cmd ci
npm.cmd start
```

Abra `http://localhost:4200`. O script do backend lê as configurações locais do `.env` e cria, se necessário, uma chave em `.jwt-secret.local`, ignorada pelo Git. Ele preserva variáveis de ambiente já definidas. O Java executado diretamente pelo IntelliJ continua exigindo a configuração explícita das variáveis abaixo.

Para exemplos fictícios de apresentação, execute o backend com `-Demo`. A carga é restrita ao perfil Spring `demo`; os acessos estão no roteiro.

## Tecnologias previstas

### Backend

- Java 25
- Spring Boot
- Maven
- Spring Web MVC
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server para validação de JWT
- Bean Validation
- Flyway
- PostgreSQL

### Frontend

- Angular
- TypeScript
- HTML
- CSS

### Infraestrutura

- Docker Compose
- PostgreSQL em container para o ambiente de desenvolvimento

## Estrutura do repositório

```text
local-services-api/
├── backend/    # aplicação Spring Boot e Maven Wrapper
├── frontend/   # aplicação Angular
├── docs/       # documentação técnica do projeto
└── README.md
```

## Pré-requisitos

Para executar o projeto localmente:

- JDK 25
- Git
- Docker com suporte ao Docker Compose
- Node.js 26.13.0 e npm 11.20.0 para o frontend

## Backend

Os comandos abaixo devem ser executados na raiz do repositório.

### Conferir Java e Maven

```powershell
java --version
.\backend\mvnw.cmd --version
```

### Compilar

```powershell
.\backend\mvnw.cmd -f backend/pom.xml compile
```

### Executar os testes

```powershell
.\backend\mvnw.cmd -f backend/pom.xml test
```

Os testes de integração dependem do PostgreSQL em execução, de `DB_PASSWORD` e `JWT_SECRET`. Para carregar o ambiente local automaticamente, execute na raiz `./scripts/Invoke-Backend.ps1 -Task Test`. Para compilar o JAR e executar os testes, use `./scripts/Invoke-Backend.ps1 -Task Package`.

### Iniciar a aplicação

```powershell
.\backend\mvnw.cmd -f backend/pom.xml spring-boot:run
```

Antes de iniciar, suba o banco com `docker compose up -d` e configure `DB_PASSWORD` e `JWT_SECRET` no terminal ou na configuração de execução do IntelliJ. `DB_PASSWORD` deve corresponder à senha do PostgreSQL. `JWT_SECRET` deve ser uma chave aleatória em Base64 que decodifique para pelo menos 32 bytes; ela é obrigatória e não deve ser versionada. Para HTTP local, `JWT_COOKIE_SECURE=false`; em HTTPS, configure `JWT_COOKIE_SECURE=true`. O `.env` do Compose não é carregado automaticamente pelo Java.

Para gerar uma chave Base64 local no PowerShell, execute e copie o resultado para a variável `JWT_SECRET` da configuração de execução:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$jwtSecret = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$jwtSecret
```

## Frontend

No terminal, dentro de `frontend`:

```powershell
npm.cmd ci
npm.cmd start
```

Acesse `http://localhost:4200`. Para gerar o build, use `npm.cmd run build`; para executar os testes uma vez, use `npm.cmd test -- --watch=false`.

Durante o desenvolvimento, chamadas `/api/...` passam pelo proxy do Angular para `http://localhost:8080`. Mantenha o backend executando e reinicie o Angular ao alterar `proxy.conf.json`. Para conferir o encaminhamento, acesse `http://localhost:4200/api/v1/health`.

## Configuração

Credenciais e segredos não são versionados. `DB_URL` e `DB_USER` são opcionais; `DB_PASSWORD` e `JWT_SECRET` são obrigatórios no processo Java. `.env` e `.jwt-secret.local` são locais e ignorados pelo Git. O `.env` é lido pelo script auxiliar; Spring Boot, sozinho, não o carrega.

## Testes de navegador

Dentro de `frontend`, instale o Firefox de testes com `npx.cmd playwright install firefox` e execute `npm.cmd run e2e`. É necessário Google Chrome instalado, Java 25, Node 26 e PostgreSQL saudável. O Playwright inicia serviços temporários nas portas 18080 e 4300; mantém os servidores usuais em 8080/4200 livres para o desenvolvimento.

O fluxo roda em Chrome e Firefox nas larguras de 360, 768 e 1366 px. Cria somente contas fictícias identificadas por UUID e as remove ao terminar. As capturas ficam em `docs/evidencias/`; o relatório local fica em `frontend/playwright-report/` (ignorado pelo Git). Logs e traces de falha também não devem ser versionados.
