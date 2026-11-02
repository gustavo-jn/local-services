# Local Services — frontend

Interface Angular do Local Services. O [README da raiz](../README.md) contém a instalação dos programas, a configuração do banco e o passo a passo completo para executar a aplicação no Windows.

## Executar

Com Node.js 26.13.0, Docker/PostgreSQL e backend já iniciados conforme o guia principal, abra um terminal nesta pasta (`frontend`):

```powershell
npm.cmd ci
npm.cmd start
```

Abra `http://localhost:4200`. Mantenha o terminal aberto. O proxy local encaminha `/api` para `http://localhost:8080`.

## Testes e build

Execute nesta pasta:

```powershell
npm.cmd test -- --watch=false
npm.cmd run build
```

Para os testes de ponta a ponta, tenha o Google Chrome instalado e o PostgreSQL saudável:

```powershell
npx.cmd playwright install firefox
npm.cmd run e2e
```

O Playwright inicia uma API temporária em `18080` e um frontend em `4300`; essas portas precisam estar livres. Também é necessário JDK 25. Os testes verificam o fluxo com API e banco reais em Chrome e Firefox. O relatório local fica em `playwright-report/`.

## Páginas disponíveis

- `/`: página inicial.
- `/cadastro`: criação de conta.
- `/login`: entrada.
- `/app`: área autenticada.
- `/app/conta`: manutenção dos dados da conta.
- `/app/perfil-profissional`: criação e edição do perfil profissional inicial.
