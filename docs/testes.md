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
- A tradução da violação de unicidade do banco foi inicialmente coberta por teste unitário com repositório simulado. A verificação posterior com transações reais está registrada abaixo.
- Como os cadastros dos testes sofrem rollback, eles não demonstram persistência de um cadastro confirmado após reiniciar o backend ou o banco. Essa verificação ainda está pendente.
- Não houve teste de frontend; a interface ainda não foi implementada.

## 01/11/2026 - Concorrência no cadastro (P2)

- Requisitos envolvidos: RF-01, RF-15 e RNF-04 no cenário de unicidade de e-mail.
- Arquivo: `CadastroUsuarioConcorrenciaTests.java`, no pacote `conta.controller` de testes.
- Comando executado dentro de `backend`, com `DB_PASSWORD` configurada: `./mvnw.cmd "-Dtest=CadastroUsuarioConcorrenciaTests" test`.
- Resultado: **1 teste, 0 falhas e 0 erros; BUILD SUCCESS**. Esta execução direcionada não repetiu os 20 testes registrados na etapa anterior.

### Cenário e evidências

- Duas requisições MockMvc executam simultaneamente em threads distintas, sem transação compartilhada de teste. Cada cadastro usa a transação real do serviço e o PostgreSQL real.
- O e-mail é exclusivo dessa execução; a segunda requisição varia caixa e espaços para verificar a normalização.
- Um componente exclusivo do teste envolve o bcrypt real e sincroniza as requisições após ambas passarem pela consulta de e-mail existente. Isso força a disputa pela restrição do banco, em vez de depender do acaso no escalonamento.
- O teste confirma dois identificadores de transação PostgreSQL distintos, uma resposta 201 e uma resposta 409 com `EMAIL_ALREADY_REGISTERED`.
- A restrição `uk_usuarios_email` rejeitou a gravação concorrente, com SQLSTATE 23505. Esse aviso no log é esperado no cenário.
- Uma consulta independente confirmou exatamente uma conta e o UUID retornado pela requisição vencedora. As respostas não continham senha nem hash.
- Após encerrar as threads, o teste removeu somente o registro com seu e-mail exclusivo. Não houve limpeza geral da tabela nem alteração de contas existentes.

### Limites

- O transporte HTTP é simulado pelo MockMvc; as transações e o banco são reais.
- O cenário verifica duas requisições concorrentes, não desempenho ou carga.
- A persistência após reiniciar a aplicação ou o banco continua pendente.
