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

- `auth`: operações de autenticação e obtenção do token CSRF.
- `config`: configuração de segurança, bcrypt e respostas dos filtros de segurança.
- `health`: verificação de disponibilidade da API.
- `common.exception`: formato comum de erro e tratamento centralizado das exceções.

As migrações SQL permanecem em `backend/src/main/resources/db/migration`. A reorganização dos pacotes Java não altera tabelas nem exige uma nova migração.
