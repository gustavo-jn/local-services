# Acompanhamento da primeira entrega

Referência: etapas P0–P6 do checklist local e RF-01 a RF-06, RF-15 e RNFs aplicáveis. A suficiência acadêmica do marco é validada pelo orientador; este documento registra a implementação e suas evidências.

| Etapa | Situação da implementação | Evidências |
|---|---|---|
| P0 | Estrutura backend/frontend, Maven Wrapper, configurações e lockfile | README, docs/versoes.md |
| P1 | PostgreSQL, volume, health check, variáveis de exemplo, Flyway, erros e segurança | compose.yaml, .env.example, migrations |
| P2 | Cadastro com BCrypt, validação, normalização e unicidade | testes de cadastro, concorrência e serviço |
| P3 | Home e cadastro Angular integrados | testes de componentes e fluxo no navegador |
| P4 | Login, consulta da conta, restauração, guard, área privada e logout com CSRF | testes JWT, integração e navegador |
| P5 | Conta editável e perfil profissional privado com agenda inicial | testes de integração e concorrência |
| P6 | Conferência final e pacote de demonstração | docs/testes.md, docs/roteiro-demo.md, docs/evidencias/ |

## Critérios finais

- [x] Operações implementadas da tela ao banco, sem simular funcionalidades futuras.
- [x] Isolamento por conta e uso da identidade do JWT no servidor.
- [x] Perfil e agenda criados na mesma transação, com unicidade no banco.
- [x] Configuração local e exemplos sem credenciais reais versionadas.
- [x] Confirmar o fluxo final em Chrome/Firefox nas larguras de 360, 768 e 1366 px.
- [x] Confirmar persistência após reiniciar banco e backend.
- [x] Registrar o resultado final de todos os testes e builds.
- [x] Consolidar a versão entregue em commit (o commit que contém este documento).
- [ ] Apresentar ao orientador e registrar a avaliação de suficiência do marco.

As verificações ainda não concluídas estão desmarcadas; a execução e as limitações ficam em `docs/testes.md`. A Parte 2 do projeto não será iniciada automaticamente.
