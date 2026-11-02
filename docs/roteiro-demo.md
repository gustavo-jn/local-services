# Roteiro da primeira demonstração

## Preparação

1. Iniciar o banco: `docker compose up -d`.
2. Iniciar a API: `./scripts/Invoke-Backend.ps1 -Task Run -Demo`.
3. Dentro de `frontend`, executar `npm.cmd ci` e `npm.cmd start` com Node 26.
4. Abrir `http://localhost:4200`.

O perfil Spring `demo` cria somente os exemplos abaixo quando ainda não existem. Sem esse perfil, não há carga automática. Não utilize essas credenciais em ambientes reais.

| Conta fictícia | E-mail | Senha inicial |
|---|---|---|
| Marina Exemplo | `marina.demo@example.com` | `Demo12345!` |
| Pedro Exemplo | `pedro.demo@example.com` | `Demo12345!` |

Pedro já possui um perfil inicial privado e uma agenda vazia. O seed não redefine senhas ou alterações feitas nessas contas; se forem modificadas durante a apresentação, usar os novos dados nos acessos seguintes.

## Apresentação

1. Apresentar a Home e explicar a proposta de serviços locais.
2. Criar uma conta fictícia nova pela interface; demonstrar campos obrigatórios e confirmação da senha.
3. Tentar cadastrar o mesmo e-mail e mostrar a mensagem de conflito.
4. Demonstrar senha incorreta no login e depois entrar corretamente.
5. Mostrar a área privada e atualizar a página para demonstrar a restauração da identificação.
6. Abrir Minha conta: alterar nome; mostrar que troca de e-mail exige senha atual; alterar o e-mail.
7. Trocar a senha; posteriormente demonstrar que a antiga é recusada e a nova funciona.
8. Criar perfil profissional e mostrar sua agenda inicial. Editar a descrição e recarregar para comprovar persistência.
9. Entrar com outra conta: ela não vê nem edita o perfil da primeira.
10. Sair; tentar abrir `/app/conta` e mostrar o encaminhamento ao login.
11. No Postman, chamar `/api/v1/auth/me` sem cookie para mostrar 401; uma alteração com JWT mas sem CSRF retorna 403.
12. Apresentar resultados registrados em `docs/testes.md` e os limites da entrega.

## Limites apresentados

Esta entrega cobre RF-01 a RF-06 e os retornos de operações RF-15, com RNFs aplicáveis. O perfil ainda é privado. Busca, catálogo, imagens de profissionais, solicitações, propostas, horários, atendimentos, chat e avaliações serão desenvolvidos nas próximas etapas. A agenda contém apenas a estrutura inicial.

Os JWTs duram 30 minutos. Logout remove os cookies do navegador; cópias de tokens já emitidos permanecem válidas até expirar. A troca de senha também não revoga tokens antigos nesse contrato inicial. Não há refresh token, recuperação de senha, pagamentos nem login social.
