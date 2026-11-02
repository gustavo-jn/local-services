# Organização do backend

O backend usa o pacote base `io.github.gustavo_jn.local_services_api` e agrupa as classes por funcionalidade. Dentro de cada funcionalidade, subpacotes separam as responsabilidades conforme surgem arquivos que os utilizem.

## Módulo de conta

```text
conta/
├── controller/
│   └── CadastroUsuarioController.java
├── dto/
│   ├── CadastroUsuarioRequestDTO.java
│   └── UsuarioResponseDTO.java
├── entity/
│   └── Usuario.java
├── exception/
│   └── EmailJaCadastradoException.java
├── repository/
│   └── UsuarioRepository.java
├── service/
│   └── UsuarioService.java
└── validation/
    ├── SenhaDentroDoLimite.java
    └── SenhaDentroDoLimiteValidator.java
```

- `controller`: requisições HTTP e encaminhamento ao serviço.
- `dto`: dados de entrada e saída, sem persistência própria.
- `entity`: entidades mapeadas para o banco por JPA.
- `exception`: exceções de negócio do módulo.
- `repository`: acesso ao banco pelo Spring Data JPA.
- `service`: regras de negócio e transações.
- `validation`: anotações e validadores de entrada.

Os testes ficam em `backend/src/test/java`, seguindo os mesmos pacotes das classes verificadas. Novos subpacotes serão criados quando houver implementação correspondente.

## Demais pacotes

- `auth`: obtenção do CSRF e endpoint de login; o serviço consulta credenciais do módulo de conta e emite o JWT.
- `config`: configuração de segurança, BCrypt, assinatura/validação do JWT, leitura do cookie HttpOnly e respostas dos filtros.
- `health`: verificação de disponibilidade da API.
- `common.exception`: formato comum de erro e tratamento centralizado das exceções.

As migrações SQL permanecem em `backend/src/main/resources/db/migration`. A reorganização dos pacotes Java não altera tabelas nem exige uma nova migração.

## Frontend - página inicial

A rota `/` renderiza `frontend/src/app/features/home/`. O HTML e o CSS foram adaptados da página independente fornecida em `site/`, na raiz, preservada como referência local.

- `home.html`: conteúdo e bindings do template Angular.
- `home.css`: estilos encapsulados da Home, preservando o layout fornecido.
- `home.ts`: sinais de estado, menu, observadores, animações e limpeza de recursos ao destruir o componente.
- `home.spec.ts`: testes de navegação local, acessibilidade do menu e ciclo de vida das interações.
- `frontend/src/styles.css`: base global de documento.
- `frontend/src/index.html`: idioma e metadados da aplicação.
- `frontend/public/images/home/`: cópias otimizadas em WebP das cinco imagens fornecidas.

O `script.js` independente não é carregado pela aplicação. Suas interações foram adaptadas para o componente, sem inserir scripts ou listeners permanentes no documento. Os links de cadastro da Home navegam para `/cadastro`; os controles de login permanecem desativados até a tela ser implementada.

## Frontend - cadastro

A rota `/cadastro` renderiza o componente em `frontend/src/app/features/auth/register/`. A tela possui campos para nome, e-mail, senha e confirmação, com validações locais alinhadas aos limites principais do backend. O serviço em `frontend/src/app/features/auth/data-access/` obtém CSRF e envia o cadastro pelo proxy local, sem incluir a confirmação de senha. A tela apresenta mensagens de sucesso e erros da API. O cadastro não inicia uma sessão.
