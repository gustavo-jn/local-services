# Registro de testes

## 01/11/2026 - Endpoint de cadastro (P2)

- Requisitos envolvidos: RF-01 e RF-15; RNF-01, RNF-03, RNF-04 e RNF-05 nos cenários abaixo. Esta verificação não representa cobertura completa desses requisitos.
- Ambiente: Java 25, Spring Boot 4.1.2 e PostgreSQL 17.10 em `localhost:5433`.
- Comando executado dentro de `backend`, com `DB_PASSWORD` configurada no processo: `./mvnw.cmd test`.
- Resultado: **20 testes, 0 falhas e 0 erros; BUILD SUCCESS**.

### Cadastro

`CadastroUsuarioControllerTests` executa oito testes com MockMvc, filtros reais de segurança, serviço real e PostgreSQL real. Cada teste usa uma transação revertida ao final.

1. Obtenção do CSRF pelo endpoint, uso do token e da sessão, cadastro com HTTP 201, UUID persistido, normalização do e-mail e ausência de senha/hash na resposta. O hash armazenado confere com a senha original, inclusive espaços nas extremidades.
2. Repetição do e-mail com variações de caixa e espaços retorna 409, mantendo um único registro.
3. Nome, e-mail e senha inválidos retornam 400 com erros por campo, sem gravação.
4. Senha acima de 72 bytes UTF-8 retorna 400, sem gravação.
5. JSON malformado retorna 400.
6. Corpo ausente retorna 400.
7. CSRF ausente retorna 403, sem gravação.
8. CSRF inválido retorna 403, sem gravação.

Também passaram os seis testes unitários de `UsuarioService`, três testes de validação da senha, dois testes do bcrypt e o teste de inicialização do contexto Spring.

### Limites e pendências

- As requisições dos testes usam MockMvc, sem abrir uma porta HTTP; a persistência usa PostgreSQL real.
- A tradução da violação de unicidade do banco foi coberta por teste unitário com repositório simulado. Ainda falta verificar duas tentativas simultâneas com transações reais.
- Como os cadastros dos testes sofrem rollback, eles não demonstram persistência de um cadastro confirmado após reiniciar o backend ou o banco. Essa verificação ainda está pendente.
- Não houve teste de frontend; a interface ainda não foi implementada.
