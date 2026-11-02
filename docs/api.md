# Contratos da API

Endereço local: `http://localhost:8080`. Os exemplos usam somente dados fictícios.

## Disponibilidade

`GET /api/v1/health` é público e retorna HTTP 200 com `{"status":"UP"}`. Ele verifica a resposta da API, sem testar o banco.

## Obtenção de CSRF

`GET /api/v1/auth/csrf` é público e retorna HTTP 200:

```json
{
  "headerName": "X-CSRF-TOKEN",
  "token": "valor-gerado-pelo-servidor"
}
```

O cliente deve preservar o cookie `JSESSIONID` recebido e enviar o token no cabeçalho indicado nas operações de alteração. Na configuração atual, o token pertence à sessão e não autentica uma conta.

## Login

`POST /api/v1/auth/login` não exige JWT, mas exige um token CSRF válido e o cookie `JSESSIONID` da mesma sessão. Enviar `Content-Type: application/json`:

```json
{
  "email": "usuario@example.com",
  "senha": "Senha123!"
}
```

Sucesso: HTTP 200, com ID, nome e e-mail no corpo. A resposta também define o cookie `LS_ACCESS_TOKEN`, com `HttpOnly`, `Path=/api`, `SameSite=Lax` e duração de 30 minutos. `Secure` é ativado quando `JWT_COOKIE_SECURE=true`; no ambiente local HTTP, use `false`. O JWT não aparece no JSON e não deve ser salvo em `localStorage`.

O token usa HS256 e contém `sub` (UUID da conta), `iss`, `aud`, `iat` e `exp`. A API valida assinatura, emissor, audiência e validade temporal. O segredo vem de `JWT_SECRET`, em Base64, com pelo menos 32 bytes depois de decodificado.

Após o login, a sessão CSRF é renovada. Antes da próxima operação de alteração, o cliente deve obter um novo token com `GET /api/v1/auth/csrf` e preservar o novo `JSESSIONID`.

| HTTP | `code` | Situação |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Corpo ou campos inválidos |
| 401 | `INVALID_CREDENTIALS` | E-mail inexistente ou senha incorreta; a resposta é igual nos dois casos |
| 403 | `INVALID_CSRF_TOKEN` | CSRF ausente, inválido ou sem a sessão correspondente |

O endpoint não cria sessão de usuário no servidor; a autenticação subsequente usa o cookie JWT. O logout ainda não está implementado.

## Usuário autenticado

`GET /api/v1/auth/me` exige um JWT válido no cookie `LS_ACCESS_TOKEN`, recebido no login. Não recebe ID ou e-mail como parâmetro: a conta é identificada pelo UUID em `sub` do JWT e consultada no banco. O endpoint não exige token CSRF, pois é uma consulta.

Sucesso: HTTP 200, com `Cache-Control: no-store` e somente os dados atuais da conta:

```json
{
  "id": "b8559bc1-8237-4345-92a8-aecb1ce1699d",
  "nome": "Usuario Teste",
  "email": "usuario@example.com"
}
```

Senha, hash e JWT não são retornados. Cookie ausente, JWT inválido ou expirado, `sub` ausente ou inválido e conta inexistente retornam HTTP 401 com `code: UNAUTHORIZED` e `message: Autenticação necessária`. O cookie `JSESSIONID` sozinho e o cabeçalho `Authorization: Bearer` não autenticam esse endpoint; a API lê o JWT somente do cookie.

Para conferir no Postman:

1. Obter CSRF e fazer `POST /api/v1/auth/login` com as credenciais de uma conta existente.
2. Preservar o cookie `LS_ACCESS_TOKEN` e enviar `GET /api/v1/auth/me` com `No Auth`, usando o mesmo host do login.
3. Conferir HTTP 200 e os dados da conta. Em um cliente sem o cookie JWT, a mesma consulta deve retornar 401.

## Cadastro de usuário

`POST /api/v1/auth/register` não exige login, mas exige CSRF válido e o cookie da sessão correspondente. Enviar `Content-Type: application/json`.

```json
{
  "nome": "Usuario Teste",
  "email": "usuario@example.com",
  "senha": "Senha123!"
}
```

- Nome obrigatório, de 2 a 120 caracteres.
- E-mail obrigatório e válido, de até 254 caracteres. Espaços nas extremidades são removidos e o valor é convertido para minúsculas antes da validação e persistência.
- Senha obrigatória, de no mínimo 8 caracteres e no máximo 72 bytes UTF-8. A senha não é aparada nem truncada.
- A conta não recebe um perfil profissional automaticamente. O cadastro também não autentica a conta.

Sucesso: HTTP 201, retornando somente ID, nome e e-mail:

```json
{
  "id": "b8559bc1-8237-4345-92a8-aecb1ce1699d",
  "nome": "Usuario Teste",
  "email": "usuario@example.com"
}
```

| HTTP | `code` | Situação |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Campos inválidos |
| 400 | `INVALID_REQUEST_BODY` | Corpo ausente ou JSON inválido |
| 403 | `INVALID_CSRF_TOKEN` | CSRF ausente, inválido ou sem a sessão correspondente |
| 409 | `EMAIL_ALREADY_REGISTERED` | E-mail já utilizado |

Os erros seguem o formato `code`, `message`, `fieldErrors`, `timestamp` e `path`. Senha e hash não são retornados.

### Testar pelo Postman

1. Executar a aplicação com PostgreSQL disponível e `DB_PASSWORD` configurada.
2. Enviar `GET /api/v1/auth/csrf` com `No Auth` e manter os cookies habilitados.
3. Copiar `token` para o cabeçalho `X-CSRF-TOKEN` de uma nova requisição.
4. Enviar `POST /api/v1/auth/register`, também com `No Auth`, usando `Body → raw → JSON` com os dados do cadastro. Utilizar o mesmo host para preservar a sessão.
5. Conferir HTTP 201. Repetir o e-mail deve retornar 409. Um cadastro manual bem-sucedido permanece no banco.
