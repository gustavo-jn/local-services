# Local Services

Plataforma para aproximar clientes e profissionais de serviços locais. Esta versão corresponde à **primeira entrega de 20% do Projeto Integrador**, com cadastro, login, área privada, edição da conta e perfil profissional inicial.

## Como executar no Windows

### 1. Instale os programas necessários

- Git.
- Java **JDK 25**, configurado no PATH.
- Node.js **26.13.0**, com npm.
- Docker Desktop, com Docker Compose.

Abra o Docker Desktop e aguarde sua inicialização. Use **PowerShell** para os comandos abaixo.

### 2. Baixe o projeto

```powershell
git clone https://github.com/gustavo-jn/local-services.git local-services-api
cd .\local-services-api
```

Se já possui o projeto, abra o terminal na raiz dele, onde está o arquivo `compose.yaml`.

### 3. Configure o banco

Na raiz do projeto:

```powershell
if (!(Test-Path .env)) { Copy-Item .env.example .env }
notepad .env
```

Defina `POSTGRES_PASSWORD`, salve e feche o arquivo. Mantenha os demais valores do exemplo. Se o banco já existir, preserve a senha original.

### 4. Inicie o banco e o backend

Na raiz do projeto:

```powershell
docker compose up -d
docker compose ps
```

Aguarde o banco aparecer como **healthy**. Depois execute:

```powershell
.\scripts\Invoke-Backend.ps1 -Task Run
```

O script carrega a configuração local e prepara a chave JWT automaticamente. Aguarde a mensagem `Started LocalServicesApiApplication` e deixe esse terminal aberto.

### 5. Inicie o frontend

Abra **outro PowerShell na raiz do projeto** e execute:

```powershell
cd .\frontend
npm.cmd ci
npm.cmd start
```

Deixe esse terminal aberto. A primeira instalação pode demorar enquanto as dependências são baixadas.

### 6. Acesse a aplicação

Abra **http://localhost:4200** no navegador. Crie uma conta e faça login para acessar a área privada.

Para encerrar, pressione **Ctrl+C** nos terminais do frontend e do backend. Na raiz, execute `docker compose stop` para parar o banco preservando os dados.
