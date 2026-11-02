# Local Services

Plataforma web para divulgação, busca e contratação de serviços locais. O projeto aproxima clientes de profissionais autônomos e contempla, em sua evolução, perfis profissionais, solicitações, propostas, agenda, acompanhamento de atendimentos, comunicação e avaliações.

## Estado atual

O backend Spring Boot executa com Java 25, PostgreSQL em Docker e migrações Flyway. Já oferece health check, obtenção de CSRF e cadastro de usuários. O frontend Angular apresenta a página inicial e a tela de cadastro em `/cadastro`, integrada ao endpoint de cadastro. O login ainda será implementado.

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

Os testes de integração dependem do PostgreSQL em execução e da variável `DB_PASSWORD` configurada no processo.

### Iniciar a aplicação

```powershell
.\backend\mvnw.cmd -f backend/pom.xml spring-boot:run
```

Antes de iniciar, suba o banco com `docker compose up -d` e configure `DB_PASSWORD` no terminal ou na configuração de execução do IntelliJ. A senha deve corresponder à do PostgreSQL. O `.env` do Compose não é carregado automaticamente pelo Java.

## Frontend

No terminal, dentro de `frontend`:

```powershell
npm.cmd ci
npm.cmd start
```

Acesse `http://localhost:4200`. Para gerar o build, use `npm.cmd run build`; para executar os testes uma vez, use `npm.cmd test -- --watch=false`.

Durante o desenvolvimento, chamadas `/api/...` passam pelo proxy do Angular para `http://localhost:8080`. Mantenha o backend executando e reinicie o Angular ao alterar `proxy.conf.json`. Para conferir o encaminhamento, acesse `http://localhost:4200/api/v1/health`.

## Configuração

Credenciais e segredos não devem ser versionados. As configurações locais do banco e, futuramente, das chaves JWT serão fornecidas por variáveis de ambiente e documentadas por arquivos de exemplo sem valores reais.
