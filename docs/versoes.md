# Versões do ambiente

Este documento registra as versões efetivamente utilizadas no desenvolvimento do Local Services. Ele deve ser atualizado quando uma ferramenta ou tecnologia for adicionada ou alterada.

Última verificação: 31/10/2026.

## Ambiente de desenvolvimento

| Componente | Versão | Situação | Observação |
|---|---:|---|---|
| Sistema operacional | Windows 11 | Verificado | Ambiente local de desenvolvimento |
| Java | 25.0.2 LTS | Verificado | Azul Zulu OpenJDK, build `25.0.2+10-LTS` |
| Maven Wrapper | 3.10.0 | Verificado | Executado por `backend/mvnw.cmd` |
| Docker | 29.6.2 | Verificado | Build `dfc4efb` |
| Docker Compose | 5.3.1 | Verificado | Disponível como `docker compose` |
| Node.js | 24.14.0 | Verificado | Será usado pelo frontend |
| npm | 11.9.0 | Verificado | Será usado pelo frontend |

## Backend

| Componente | Versão | Situação | Origem |
|---|---:|---|---|
| Spring Boot | 4.1.2 | Configurado | Parent do `backend/pom.xml` |
| Spring Security | 7.1.2 | Resolvido | Gerenciado pelo Spring Boot 4.1.2 |
| Java de compilação | 25 | Configurado | Propriedade `java.version` do Maven |
| PostgreSQL JDBC | 42.7.14 | Resolvido | Gerenciado pelo Spring Boot 4.1.2 |
| Flyway | Gerenciado pelo Spring Boot | Configurado | Versão não fixada diretamente no projeto |

## Banco de dados

| Componente | Versão | Situação | Observação |
|---|---:|---|---|
| PostgreSQL | A definir | Pendente | A imagem será fixada no `compose.yaml`; referência inicial: família 17 |

## Frontend

| Componente | Versão | Situação | Observação |
|---|---:|---|---|
| Angular | A definir | Pendente | Será definida ao criar o projeto em `frontend/` |
| TypeScript | A definir | Pendente | Será definida pela versão escolhida do Angular |

## Comandos de verificação

Executar na raiz do repositório:

```powershell
java --version
.\backend\mvnw.cmd --version
docker --version
docker compose version
node --version
npm --version
```

As versões gerenciadas pelo Spring Boot podem ser conferidas pela árvore de dependências do Maven:

```powershell
.\backend\mvnw.cmd dependency:tree
```
