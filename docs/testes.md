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

## 01/11/2026 - Integração da Home fornecida em `site/` (P3)

- A página independente foi adaptada ao componente Angular existente na rota `/`.
- Comandos executados dentro de `frontend`: `npm.cmd test -- --watch=false` e `npm.cmd run build`.
- Resultado final: **2 arquivos, 9 testes aprovados; build de produção aprovado**. Os testes usam Vitest/jsdom, não um navegador real.
- Os testes da Home verificam título e imagens locais, abertura do menu, fechamento por Escape com retorno de foco, fechamento por âncora/clique externo/mudança de viewport, indisponibilidade dos destinos ainda não implementados, troca de fotografia, pausa, movimento reduzido e limpeza dos observadores e intervalos.
- No teste de foco, o botão é explicitamente exibido porque o jsdom não aplica a largura móvel às media queries CSS. Isso não comprova a renderização visual do menu em celular.
- A primeira tentativa de build revelou o limite padrão de 8 kB por componente. O limite foi revisto conforme `docs/decisoes.md`; o build final passou sem erros. Uma falha inicial do teste de foco foi corrigida ajustando a representação do botão móvel no jsdom.
- Um servidor HTTP estático temporário serviu `frontend/dist/local-services/browser` em `127.0.0.1:4302`: `/` retornou 200 e as cinco imagens retornaram 200 com `Content-Type: image/webp`.
- Conferidos os destinos das âncoras internas e a existência e integridade das imagens referenciadas. As imagens otimizadas foram abertas para inspeção e os textos alternativos ajustados ao conteúdo.
- O material original em `site/` foi preservado. Nenhuma mudança no backend, no esquema ou nos dados do PostgreSQL.

### Limites de verificação

- A inspeção visual da página nas larguras 360, 768 e 1366 px ficou pendente: o acesso ao navegador embutido estava indisponível e a ferramenta de uso do computador interrompeu a inspeção por não conseguir determinar a URL atual do Chrome com segurança. Não foi contornado esse bloqueio por outra automação de navegador.
- Build, testes e HTTP não comprovam ausência de sobreposição ou rolagem horizontal em um navegador real. A conferência visual e o teste em Chrome/Firefox ainda precisam ser feitos.
- Cadastro e login no frontend seguem pendentes. Os botões estão desativados; não houve teste de autenticação ou cadastro pela interface.

## 02/11/2026 - Conclusão técnica da primeira entrega P0–P6

Este registro atualiza os limites dos registros históricos acima. Cadastro, login, estado autenticado, logout, conta e perfil estão integrados à API real.

### Backend e banco reais

- Na raiz: `./scripts/Invoke-Backend.ps1 -Task Test`: **49 testes, 0 falhas, 0 erros; BUILD SUCCESS**.
- Na raiz: `./scripts/Invoke-Backend.ps1 -Task Package`: repetiu os **49 testes aprovados** e gerou o JAR executável `backend/target/local-services-api-0.0.1-SNAPSHOT.jar`.
- Java 25.0.2, Spring Boot 4.1.2, PostgreSQL 17.10 e migrações V1/V2. Os scripts carregaram as configurações locais sem imprimir senhas ou a chave JWT.
- `UsuarioAutenticadoControllerTests`: 17 cenários com filtro e validação JWT reais; apenas repository simulado. Verifica assinatura, expiração, emissor, audiência, ausência de `aud`/`sub`, conta inexistente, ausência de cookie e não exposição de credenciais.
- `EntregaInicialTests`: 10 cenários com PostgreSQL real e rollback. Verifica cookie HttpOnly/Path/SameSite/validade, login inválido, rotação de sessão/CSRF, logout, rejeição de alteração sem CSRF mesmo com JWT, edição de nome/e-mail/senha, conflito de e-mail, senha anterior recusada, perfil/agenda, privacidade e isolamento de contas.
- `PerfilConcorrenciaTests`: duas transações reais simultâneas retornaram 201 e 409, mantendo um perfil e uma agenda. Remove somente seus próprios registros de teste.
- Os testes anteriores de cadastro, conflito concorrente, validação, BCrypt e inicialização também passaram.

Durante a implementação, os testes detectaram a dispensa automática de CSRF para bearer tokens pelo Resource Server. O matcher do filtro foi corrigido para exigir CSRF em todos os métodos inseguros, inclusive com JWT em cookie. Foi corrigido também o reconhecimento de caminhos públicos pelo resolver em requisições MockMvc. A suíte completa passou após as correções.

### Frontend

- Dentro de `frontend`, com Node 26.13.0: `npm.cmd run build`: **build de produção aprovado**, total inicial 420,10 kB (estimativa transferida 99,25 kB), dentro dos orçamentos configurados.
- `npm.cmd test -- --watch=false`: **5 arquivos, 22 testes aprovados**, sem falhas.
- Vitest/jsdom verifica Home, aplicação, formulário de login, restauração/saída da identidade e criação/edição do perfil. Esses testes usam HTTP simulado; as verificações reais estão descritas a seguir.

### Navegadores e integração real

- `npm.cmd run e2e`: execução final com **6 cenários aprovados**, usando Google Chrome 156.0.8078.74 e Firefox 157.0 nas larguras **360, 768 e 1366 px**.
- API temporária em 18080, Angular temporário em 4300, PostgreSQL real. O Playwright iniciou e encerrou os serviços temporários, sem encerrar o IntelliJ ou o servidor habitual de desenvolvimento.
- Cada cenário percorreu: Home; cadastro válido; rejeição de e-mail duplicado; senha incorreta; login correto; cookie HttpOnly inacessível a `document.cookie`; restauração após reload; troca de nome/e-mail/senha; criação e edição de perfil; consulta após reload; tentativa de alteração sem CSRF; logout; bloqueio da rota privada; senha antiga recusada; nova senha aceita; encerramento do estado da SPA após 401 em operação privada.
- As contas usam prefixo `e2e-` e UUID exclusivo por execução; a limpeza elimina somente essas contas e seus perfis/agendas.
- A primeira execução encontrou rolagem horizontal na área privada de 360 px. A grade e o tamanho mínimo da navegação foram corrigidos. Os dois cenários móveis passaram na repetição, e depois a matriz final inteira passou.
- Capturas reais de Home, login, área privada, conta e perfil, em `docs/evidencias/`. Inspeção visual adicional das capturas de desktop e celular confirmou leitura e disposição dos formulários. Os testes verificam ausência de transbordamento horizontal nas páginas capturadas; não representam auditoria completa de acessibilidade.

### Persistência após reinício

- Na raiz: `./scripts/Test-Persistencia.ps1`: **aprovado**.
- O script executou o JAR em 18080, criou conta/perfil/agenda fictícios, encerrou somente essa instância, aplicou o Compose com imagem fixada, reiniciou o PostgreSQL e iniciou novamente a API.
- O volume permaneceu `local-services-api_postgres_data`. Login e consulta após os reinícios retornaram os mesmos UUIDs de conta, perfil e agenda.
- O registro fictício da execução final foi `59eaca23-246e-410a-ab3a-d57caec1d2d1`; o script removeu somente essa conta e seus vínculos ao terminar. Nunca removeu o volume nem limpou tabelas inteiras.
- O perfil `demo` foi executado em duas inicializações. As contas fictícias Marina/Pedro foram preparadas sem duplicação ou redefinição de dados; permaneceram disponíveis para a apresentação. Sem esse perfil, não há carga automática.
- A primeira tentativa do helper falhou na leitura do nome do volume por aspas em um template Docker no PowerShell; a consulta passou a usar JSON. A execução corrigida confirmou a persistência e encerrou a API temporária.

### Artefatos e limites

- Coleção Postman JSON 2.1: 13 requisições, variáveis de credenciais vazias, obtenção de CSRF para alterações e verificações de status/ausência de credenciais na resposta. JSON e estrutura das operações conferidos; a coleção não foi executada pelo Postman CLI. A execução real dos contratos foi feita pelos testes Spring e pelos navegadores.
- `.env` e `.jwt-secret.local` confirmados como ignorados pelo Git; segredos locais não integram o pacote entregue.
- Logs detalhados, relatório HTML e traces ficam em diretórios ignorados (`backend/target`, `frontend/playwright-report`, `frontend/test-results`). Capturas versionadas contêm somente dados fictícios.
- Escopo funcional encerrado em P6; perfil público, horários, busca, catálogo, solicitações, propostas, atendimentos, mensagens e avaliações continuam para a Parte 2. Aceitação acadêmica do marco depende do orientador.
