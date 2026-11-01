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
- Como os cadastros desses testes sofrem rollback, eles não demonstram persistência após reinício. A verificação posterior de reinício do backend está registrada abaixo; o reinício do banco continua pendente.
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
- A persistência após reiniciar o backend foi verificada posteriormente, conforme o registro abaixo. Reiniciar o banco ainda não foi verificado.

## 01/11/2026 - Persistência após reiniciar o backend (P2)

- Verificação manual automatizada por PowerShell, com HTTP real na porta temporária 18080 e PostgreSQL real em `localhost:5433`.
- As instâncias temporárias foram iniciadas dentro de `backend`, com `DB_PASSWORD` configurada, usando `./mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=18080"`.
- Após a primeira inicialização (PID 19620), foram obtidos CSRF e sessão e enviado um cadastro fictício com `Invoke-WebRequest`. Resultado: HTTP 201.
- Uma consulta com `docker compose exec -T db psql -U local_services -d local_services` confirmou o registro pelo UUID retornado: `5b30e99c-1ada-4ed0-a5f6-15acf8a85548`.
- A primeira instância foi encerrada com Ctrl+C; foi confirmado que o processo terminou e a porta 18080 ficou livre.
- Uma nova instância foi iniciada com o mesmo comando (PID 26264), sem recriar ou reiniciar o banco.
- Após obter nova sessão e novo CSRF, a repetição do cadastro retornou HTTP 409. Uma consulta direta confirmou o mesmo UUID persistido.
- Limpeza: foi removido somente o registro fictício, filtrando por seu UUID e e-mail exclusivo. Resultado: `DELETE 1`; a consulta posterior pelo UUID retornou contagem zero.
- A segunda instância temporária foi encerrada. A aplicação do IntelliJ e os demais cadastros não foram alterados.

**Resultado:** o cadastro confirmado permanece após reiniciar o processo do backend. Nenhuma alteração no código ou no esquema do banco foi necessária.

**Limite:** o container PostgreSQL não foi reiniciado; esta verificação não comprova persistência após reinício ou recriação do banco. Os logs e os metadados fictícios desta execução ficaram em `backend/target`, ignorado pelo Git.

## 01/11/2026 - Estrutura Angular criada manualmente (P3)

- O responsável criou o projeto pelo Angular CLI e moveu seu conteúdo para `frontend`. A pasta adicional `local-services` e o `.gitkeep` foram removidos manualmente.
- Configuração: componentes standalone, roteamento, CSS, sem SSR e sem integração adicional de ferramentas de IA.
- Ambiente: Node 26.13.0 e npm 11.20.0. Lockfile conferido: Angular 22.2.4, Angular CLI/Build 22.2.5, TypeScript 6.0.3 e Vitest 5.0.5.
- `npm.cmd test -- --watch=false`: executado pelo responsável em `frontend`, com saída compartilhada na conversa; **1 arquivo e 2 testes aprovados**, sem falhas (15:56:53, horário local).
- `npm.cmd run build`: executado pelo assistente em `frontend`; **build de produção aprovado**, com saída em `dist/local-services` (15:57:34, horário local).
- Confirmado que não existe `frontend/.git` e que dependências, build e cache estão ignorados pelo Git.

**Limites:** os testes verificam somente a criação do componente principal e o título da página padrão, usando Vitest no ambiente Node/jsdom. Não há telas do Local Services, integração com a API ou verificação de responsividade. A etapa P3 continua em andamento.

## 01/11/2026 - HttpClient e proxy local (P3)

- Habilitado `provideHttpClient()` em `frontend/src/app/app.config.ts`.
- Adicionado `frontend/proxy.conf.json`: `/api/**` encaminha para `http://localhost:8080`.
- Configurado `proxyConfig` nas opções de `serve` em `frontend/angular.json`.
- `npm.cmd run build`: executado pelo assistente após habilitar o HttpClient; build aprovado.
- Os arquivos JSON do proxy e do workspace foram lidos e validados após as alterações.
- O responsável reiniciou o Angular e confirmou o funcionamento de `http://localhost:4200/api/v1/health`, conforme o teste manual orientado na conversa. Esse teste HTTP não foi repetido pelo assistente.

**Limites:** confirmado apenas o encaminhamento para o health check. A configuração é do servidor de desenvolvimento; ainda não há telas, envio de cadastro pelo frontend ou fluxo CSRF integrado no Angular.
