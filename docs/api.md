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
