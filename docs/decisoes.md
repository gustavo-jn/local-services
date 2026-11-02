# Decisões de implementação

## 01/11/2026 - Integração da página inicial independente

- A referência fornecida está em `site/`, na raiz do repositório. Seus arquivos foram preservados e não são usados diretamente como uma segunda aplicação.
- O conteúdo foi integrado à Home standalone existente, mantendo a estrutura visual e as imagens fornecidas.
- Estilos do documento (`html`, `body`) foram separados dos estilos encapsulados da Home. As variáveis do componente usam `:host`, permitindo que funcionem com o encapsulamento do Angular.
- As interações do script independente usam sinais e bindings Angular. Observadores, listeners e temporizadores são encerrados ao destruir a Home; os ciclos visuais param quando a seção sai de vista, quando a aba fica oculta ou quando o usuário pausa o movimento.
- Foi adicionado controle explícito de pausa e respeitada a preferência por movimento reduzido. Fotos e textos permanecem disponíveis sem suporte a IntersectionObserver.
- Cadastro e login não foram simulados nem ampliados nesta tarefa. Seus botões ficam desativados, com indicação de indisponibilidade, até implementar as rotas correspondentes.
- As mensagens sobre etapas técnicas do projeto foram substituídas por conteúdo público sobre a proposta da plataforma.
- As imagens originais totalizam 15.313.489 bytes. Cópias em WebP, limitadas a 1920 px de largura para o fundo e 1000 px para as demais imagens, totalizam 524.222 bytes: redução aproximada de 96,6%. Os originais não foram alterados.
- A primeira compilação mediu 22,43 kB de CSS encapsulado para a Home, acima do limite padrão. Para acomodar o layout fornecido, o orçamento `anyComponentStyle` foi alterado de 4/8 kB para 24/28 kB (aviso/erro). Após os ajustes finais, o build passou sem aviso desse orçamento. O orçamento total dos arquivos iniciais permaneceu em 500 kB/1 MB.
- Não foram adicionadas bibliotecas, fontes remotas ou dependências ao projeto. A Home continua utilizando as fontes de sistema do material fornecido.

## 02/11/2026 - Login por JWT em cookie

- O backend emite JWT assinado com HS256; a chave Base64 obrigatória vem de `JWT_SECRET` e deve decodificar para pelo menos 32 bytes. A dependência OAuth2 Resource Server já existente fornece emissão e validação JWT, sem nova biblioteca.
- O token dura 30 minutos, identifica a conta pelo UUID em `sub` e inclui `iss`, `aud`, `iat` e `exp`. A validação confere assinatura, algoritmo, emissor, audiência e validade temporal.
- O token é enviado somente no cookie `LS_ACCESS_TOKEN` (`HttpOnly`, `Path=/api`, `SameSite=Lax`). `Secure` é configurável: falso no HTTP local e verdadeiro em HTTPS.
- O endpoint de login continua protegido por CSRF. Após autenticar, o identificador da sessão e o token CSRF são renovados; o cliente deve buscar um novo CSRF antes da próxima alteração.
- E-mail inexistente e senha incorreta retornam a mesma resposta 401. O endpoint não devolve o JWT no corpo.
- Consulta de usuário autenticado, login na interface e logout foram implementados na conclusão da primeira entrega, descrita abaixo.

## 02/11/2026 - Conclusão do escopo P0–P6

- A identidade é restaurada via `/auth/me`; a área `/app` usa guard no Angular e a autorização continua no servidor. O contrato não usa refresh token ou armazenamento JWT acessível ao JavaScript.
- Logout é público quanto ao JWT para permitir sair mesmo após expiração, mas exige CSRF. Expira os cookies e invalida a sessão CSRF. Tokens copiados antes do logout/troca de senha continuam válidos até expirar; revogação é uma evolução separada.
- O filtro CSRF recebe explicitamente o matcher padrão de métodos inseguros após a configuração. Isso impede a dispensa automática de CSRF que o Resource Server aplicaria a requisições reconhecidas pelo bearer resolver. Referência: [implementação oficial do Resource Server](https://github.com/spring-projects/spring-security/blob/main/config/src/main/java/org/springframework/security/config/annotation/web/configurers/oauth2/server/resource/OAuth2ResourceServerConfigurer.java). Testes verificam CSRF também com cookie JWT válido.
- Alteração de e-mail e senha exige senha atual; mudanças sensíveis da mesma conta são serializadas com bloqueio pessimista. O e-mail continua protegido pela restrição única do banco.
- Perfil profissional é opcional, privado e único por conta. A agenda é criada junto, em uma transação, no fuso `America/Sao_Paulo`. O titular não é aceito como entrada. A criação concorrente é serializada pela conta e reforçada por unicidade no banco.
- Nome profissional: 2–120; descrição: 10–3000 caracteres, após trim. Aceite padrão verdadeiro; publicação falsa. Não foram antecipados catálogo, horários ou publicação.
- PostgreSQL 17.10 foi fixado pelo digest da imagem já validada; V2 apenas acrescenta tabelas. O helper local lê `.env` e mantém uma chave aleatória em `.jwt-secret.local`, sem expor valores nos logs. Execução direta pelo IntelliJ continua usando as variáveis do processo.
- Dados fictícios são restritos ao perfil Spring `demo` e não redefinem contas existentes. Testes de navegador usam dados exclusivos por execução, com limpeza limitada a esses identificadores.
- Testes Playwright são locais e não publicam dados em serviços externos. Chrome instalado e Firefox do Playwright verificam larguras de 360, 768 e 1366 px.
