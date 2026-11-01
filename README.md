# Local Services

Plataforma web para divulgação, busca e contratação de serviços locais. O projeto aproxima clientes de profissionais autônomos e contempla, em sua evolução, perfis profissionais, solicitações, propostas, agenda, acompanhamento de atendimentos, comunicação e avaliações.

## Estado atual

O projeto está na etapa inicial de organização e configuração. Neste momento, o backend Spring Boot compila com Java 25, mas a conexão com o PostgreSQL, as migrações, as funcionalidades da API e o frontend ainda serão implementados.

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
├── frontend/   # reservado para a aplicação Angular
├── docs/       # documentação técnica do projeto
└── README.md
```

## Pré-requisitos

Para trabalhar com o estado atual do backend:

- JDK 25
- Git

Para as próximas etapas também será necessário:

- Docker com suporte ao Docker Compose
- Node.js e npm, quando o frontend Angular for criado

## Backend

Os comandos abaixo devem ser executados na raiz do repositório.

### Conferir Java e Maven

```powershell
java --version
.\backend\mvnw.cmd --version
```

### Compilar

```powershell
.\backend\mvnw.cmd compile
```

### Executar os testes

```powershell
.\backend\mvnw.cmd test
```

Os testes que carregam o contexto completo do Spring dependem da configuração do banco de dados. Enquanto o PostgreSQL e as propriedades JDBC não forem configurados, esses testes poderão falhar durante a criação do `DataSource`.

### Iniciar a aplicação

```powershell
.\backend\mvnw.cmd spring-boot:run
```

A inicialização completa também depende da configuração do PostgreSQL, que será adicionada na etapa de infraestrutura do projeto.

## Configuração

Credenciais e segredos não devem ser versionados. As configurações locais do banco e, futuramente, das chaves JWT serão fornecidas por variáveis de ambiente e documentadas por arquivos de exemplo sem valores reais.
