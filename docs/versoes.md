# Versões do ambiente

Este documento registra as versões efetivamente utilizadas no desenvolvimento do Local Services. Ele deve ser atualizado quando uma ferramenta ou tecnologia for adicionada ou alterada.

Última atualização: 01/11/2026. As versões do frontend foram conferidas no lockfile; os registros anteriores do ambiente foram preservados.

## Ambiente de desenvolvimento

| Componente | Versão | Situação | Observação |
|---|---:|---|---|
| Sistema operacional | Windows 11 | Verificado | Ambiente local de desenvolvimento |
| Java | 25.0.2 LTS | Verificado | Azul Zulu OpenJDK, build `25.0.2+10-LTS` |
| Maven Wrapper | 3.10.0 | Verificado | Executado por `backend/mvnw.cmd` |
| Docker | 29.6.2 | Verificado | Build `dfc4efb` |
| Docker Compose | 5.3.1 | Verificado | Disponível como `docker compose` |
| Node.js | 26.13.0 LTS | Verificado | Instalado e selecionado pelo fnm |
| npm | 11.20.0 | Verificado | Registrado também em `frontend/package.json` |

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
| PostgreSQL | 17.10 | Verificado | Observado nos testes do backend; o Compose ainda usa a tag móvel `postgres:17` |

## Frontend

| Componente | Versão | Situação | Observação |
|---|---:|---|---|
| Angular | 22.2.4 | Instalado | Versão resolvida em `frontend/package-lock.json` |
| Angular CLI / Build | 22.2.5 | Instalado | Ferramentas locais do frontend; CLI também instalado globalmente |
| TypeScript | 6.0.3 | Instalado | Versão resolvida no lockfile |
| RxJS | 7.8.2 | Instalado | Versão resolvida no lockfile |
| Vitest | 5.0.5 | Instalado | Executor dos testes iniciais |

## Comandos de verificação

Executar na raiz do repositório:

```powershell
java --version
.\backend\mvnw.cmd --version
docker --version
docker compose version
node --version
npm.cmd --version
```

As versões gerenciadas pelo Spring Boot podem ser conferidas pela árvore de dependências do Maven:

```powershell
.\backend\mvnw.cmd -f backend/pom.xml dependency:tree
```

Para conferir as versões do frontend, executar dentro de `frontend`:

```powershell
npm.cmd exec -- ng version
npm.cmd ls --depth=0
```

O `package.json` mantém os intervalos gerados pelo Angular CLI. O `package-lock.json` registra as versões resolvidas; em um novo clone, usar `npm.cmd ci` para reproduzir essa instalação. Dependências, build e cache não são versionados.
