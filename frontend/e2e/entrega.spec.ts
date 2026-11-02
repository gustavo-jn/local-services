import { test, expect, Page } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { readFileSync, mkdirSync } from 'node:fs';
import path from 'node:path';

const repo = path.resolve(process.cwd(), '..');

async function conferirLayout(page: Page) {
  expect(
    await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1),
  ).toBe(true);
}

test('fluxo real da entrega: cadastro, login, conta, perfil, persistência e saída', async ({
  page,
  context,
}, testInfo) => {
  const runId = randomUUID();
  const email = `e2e-${runId}-conta@example.com`;
  const novoEmail = `e2e-${runId}-novo@example.com`;
  const senha = 'SenhaExemplo123!';
  const novaSenha = 'NovaSenhaExemplo123!';
  const evidence = path.join(repo, 'docs', 'evidencias');
  mkdirSync(evidence, { recursive: true });
  const captura = async (nome: string) => {
    await conferirLayout(page);
    await page.screenshot({
      path: path.join(evidence, `${testInfo.project.name}-${nome}.png`),
      fullPage: true,
    });
  };
  try {
    console.log(`Navegador: ${testInfo.project.name}; versão: ${context.browser()?.version()}`);
    await page.goto('/');
    await expect(page.getByRole('heading', { level: 1 })).toContainText('Serviços presenciais');
    await captura('home');
    // A mesma navegação existe no menu móvel e no cabeçalho desktop.
    await page.goto('/cadastro');
    await page.getByLabel('Nome completo').fill('Teste Entrega');
    await page.getByLabel('E-mail', { exact: true }).fill(email);
    await page.getByLabel('Senha', { exact: true }).fill(senha);
    await page.getByLabel('Confirme sua senha').fill(senha);
    await page.getByRole('button', { name: 'Criar conta', exact: true }).click();
    await expect(page).toHaveURL(/\/login$/);
    await captura('login');

    await page.goto('/cadastro');
    await page.getByLabel('Nome completo').fill('Teste Entrega');
    await page.getByLabel('E-mail', { exact: true }).fill(email);
    await page.getByLabel('Senha', { exact: true }).fill(senha);
    await page.getByLabel('Confirme sua senha').fill(senha);
    await page.getByRole('button', { name: 'Criar conta', exact: true }).click();
    await expect(page.getByRole('alert')).toContainText('já está cadastrado');

    await page.goto('/login');
    await page.getByLabel('E-mail', { exact: true }).fill(email);
    await page.getByLabel('Senha', { exact: true }).fill('SenhaIncorreta123!');
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await expect(page.getByRole('alert')).toContainText('E-mail ou senha incorretos');
    await page.getByLabel('Senha', { exact: true }).fill(senha);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await expect(page).toHaveURL(/\/app$/);
    await expect(page.getByRole('heading', { name: 'Olá, Teste Entrega.' })).toBeVisible();
    const cookie = (await context.cookies()).find((cookie) => cookie.name === 'LS_ACCESS_TOKEN');
    expect(cookie?.httpOnly).toBe(true);
    expect(cookie?.path).toBe('/api');
    expect(await page.evaluate(() => document.cookie.includes('LS_ACCESS_TOKEN'))).toBe(false);
    await page.reload();
    await expect(page.getByRole('heading', { name: 'Olá, Teste Entrega.' })).toBeVisible();
    await captura('area');

    await page.getByRole('link', { name: 'Minha conta', exact: true }).first().click();
    await page.getByLabel('Nome completo').fill('Teste Editado');
    await page.getByRole('button', { name: 'Salvar nome' }).click();
    await expect(page.getByRole('status')).toContainText('Dados atualizados');
    await page.getByLabel('E-mail', { exact: true }).fill(novoEmail);
    await page.locator('#email-senha').fill(senha);
    await page.getByRole('button', { name: 'Alterar e-mail' }).click();
    await expect(page.getByRole('status')).toContainText('Dados atualizados');
    await page.locator('#senha-atual').fill(senha);
    await page.getByLabel('Nova senha', { exact: true }).fill(novaSenha);
    await page.getByLabel('Confirme a nova senha').fill(novaSenha);
    await page.getByRole('button', { name: 'Alterar senha' }).click();
    await expect(page.getByRole('status')).toContainText('Senha alterada');
    await captura('conta');

    await page.getByRole('link', { name: 'Perfil profissional', exact: true }).click();
    await page.getByLabel('Nome profissional').fill('Trabalho de exemplo');
    await page
      .getByLabel('Sobre seu trabalho')
      .fill('Experiência em serviços locais, com cuidado e atenção em cada atendimento.');
    await page.getByRole('button', { name: 'Criar meu perfil' }).click();
    await expect(page.getByRole('status')).toContainText('Perfil profissional criado');
    await expect(page.getByRole('heading', { name: 'Agenda inicial criada' })).toBeVisible();
    await page
      .getByLabel('Sobre seu trabalho')
      .fill('Descrição editada e persistida para a demonstração da entrega inicial.');
    await page.getByLabel('Estou aceitando novos serviços').uncheck();
    await page.getByRole('button', { name: 'Salvar alterações' }).click();
    await expect(page.getByRole('status')).toContainText('Perfil atualizado');
    await page.reload();
    await expect(page.getByLabel('Sobre seu trabalho')).toHaveValue(
      'Descrição editada e persistida para a demonstração da entrega inicial.',
    );
    await expect(page.getByLabel('Estou aceitando novos serviços')).not.toBeChecked();
    await captura('perfil');
    const semCsrf = await context.request.patch('/api/v1/me', {
      data: { nome: 'Tentativa sem CSRF' },
    });
    expect(semCsrf.status()).toBe(403);

    await page.getByRole('button', { name: 'Sair', exact: true }).click();
    await expect(page).toHaveURL('http://localhost:4300/');
    expect((await context.request.get('/api/v1/auth/me')).status()).toBe(401);
    await page.goto('/app/conta');
    await expect(page).toHaveURL(/\/login\?motivo=sessao/);
    await page.getByLabel('E-mail', { exact: true }).fill(novoEmail);
    await page.getByLabel('Senha', { exact: true }).fill(senha);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await expect(page.getByRole('alert')).toContainText('E-mail ou senha incorretos');
    await page.getByLabel('Senha', { exact: true }).fill(novaSenha);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await expect(page).toHaveURL(/\/app$/);
    await expect(page.getByRole('heading', { name: 'Olá, Teste Editado.' })).toBeVisible();
    // Uma sessão recusada pela API também precisa encerrar o estado da SPA.
    const atual = (await context.cookies()).find((cookie) => cookie.name === 'LS_ACCESS_TOKEN');
    if (!atual) throw new Error('Cookie de autenticação ausente após novo login');
    await context.addCookies([{ ...atual, value: 'token-invalido-para-teste' }]);
    await page.getByRole('link', { name: 'Minha conta', exact: true }).first().click();
    await expect(page).toHaveURL(/\/login\?motivo=sessao/);
  } finally {
    // Só remove registros da execução atual, identificados pelo UUID gerado acima.
    const values = Object.fromEntries(
      readFileSync(path.join(repo, '.env'), 'utf8')
        .split(/\r?\n/)
        .filter((line) => /^[A-Z_]+=/.test(line))
        .map((line) => {
          const at = line.indexOf('=');
          return [line.slice(0, at), line.slice(at + 1).replace(/^['"]|['"]$/g, '')];
        }),
    );
    const match = `email LIKE 'e2e-${runId}-%@example.com'`;
    execFileSync(
      'docker',
      [
        'compose',
        'exec',
        '-T',
        'db',
        'psql',
        '-U',
        values.POSTGRES_USER || 'local_services',
        '-d',
        values.POSTGRES_DB || 'local_services',
        '-v',
        'ON_ERROR_STOP=1',
      ],
      {
        cwd: repo,
        windowsHide: true,
        input: `BEGIN; DELETE FROM agendas WHERE perfil_profissional_id IN (SELECT p.id FROM perfis_profissionais p JOIN usuarios u ON u.id=p.usuario_id WHERE ${match}); DELETE FROM perfis_profissionais WHERE usuario_id IN (SELECT id FROM usuarios WHERE ${match}); DELETE FROM usuarios WHERE ${match}; COMMIT;`,
        stdio: ['pipe', 'pipe', 'pipe'],
      },
    );
  }
});
