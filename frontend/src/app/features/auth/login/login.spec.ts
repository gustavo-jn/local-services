import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;
  let navigate: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    http = TestBed.inject(HttpTestingController);
    navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
    fixture.destroy();
    vi.restoreAllMocks();
  });

  function preencher(email = 'exemplo@email.com', senha = 'Senha123!'): void {
    const host: HTMLElement = fixture.nativeElement;
    for (const [id, valor] of [['login-email', email], ['login-senha', senha]]) {
      const input = host.querySelector<HTMLInputElement>(`#${id}`)!;
      input.value = valor;
      input.dispatchEvent(new Event('input', { bubbles: true }));
    }
  }

  function enviar(): void {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
  }

  function responderCsrf(token = 'csrf-teste'): void {
    const request = http.expectOne('/api/v1/auth/csrf');
    expect(request.request.method).toBe('GET');
    request.flush({ headerName: 'X-CSRF-TOKEN', token });
  }

  it('validates empty or malformed fields before making any HTTP request', async () => {
    enviar();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Informe seu e-mail.');
    expect(fixture.nativeElement.textContent).toContain('Informe sua senha.');
    expect(document.activeElement?.id).toBe('login-email');

    preencher('email-invalido');
    enviar();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Digite um e-mail válido.');
    http.expectNone('/api/v1/auth/csrf');
    http.expectNone('/api/v1/auth/login');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('obtains CSRF before posting credentials, prevents duplicate submissions and redirects only after success', async () => {
    preencher('  Exemplo@Email.com  ', ' Senha123! ');
    enviar();
    enviar();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('fieldset').disabled).toBe(true);
    expect(fixture.nativeElement.querySelector('button[type="submit"]').textContent).toContain('Entrando');
    http.expectNone('/api/v1/auth/login');
    expect(navigate).not.toHaveBeenCalled();

    responderCsrf();
    const request = http.expectOne('/api/v1/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.get('X-CSRF-TOKEN')).toBe('csrf-teste');
    expect(request.request.body).toEqual({ email: 'exemplo@email.com', senha: ' Senha123! ' });
    expect(navigate).not.toHaveBeenCalled();
    request.flush({ id: 'id-teste', nome: 'Exemplo', email: 'exemplo@email.com' });

    await fixture.whenStable();
    expect(navigate).toHaveBeenCalledWith('/app');
    expect(fixture.componentInstance.formulario.controls.senha.value).toBe('');
    expect(fixture.nativeElement.querySelector('fieldset').disabled).toBe(false);
  });

  it('keeps the user on the form after invalid credentials and fetches a fresh CSRF on retry', async () => {
    preencher();
    enviar();
    responderCsrf('csrf-primeira-tentativa');
    http.expectOne('/api/v1/auth/login').flush(
      { code: 'INVALID_CREDENTIALS', message: 'E-mail ou senha inválidos' },
      { status: 401, statusText: 'Unauthorized' },
    );

    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('E-mail ou senha incorretos');
    expect(navigate).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('fieldset').disabled).toBe(false);

    preencher('exemplo@email.com', 'SenhaCorrigida123!');
    enviar();
    responderCsrf('csrf-segunda-tentativa');
    const retry = http.expectOne('/api/v1/auth/login');
    expect(retry.request.headers.get('X-CSRF-TOKEN')).toBe('csrf-segunda-tentativa');
    retry.flush({ id: 'id-teste', nome: 'Exemplo', email: 'exemplo@email.com' });
    await fixture.whenStable();
    expect(navigate).toHaveBeenCalledTimes(1);
  });

  it('does not send credentials when CSRF cannot be obtained', async () => {
    preencher();
    enviar();
    http.expectOne('/api/v1/auth/csrf').error(new ProgressEvent('error'));
    await fixture.whenStable();
    http.expectNone('/api/v1/auth/login');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('Não foi possível conectar ao servidor');
    expect(fixture.componentInstance.enviando()).toBe(false);
    expect(navigate).not.toHaveBeenCalled();
  });

  it.each([
    [400, 'Confira o e-mail e a senha'],
    [403, 'Sua sessão de segurança expirou'],
    [503, 'Não foi possível entrar agora'],
  ])('handles HTTP %s without redirecting or exposing server content', async (status, mensagem) => {
    preencher();
    enviar();
    responderCsrf();
    http.expectOne('/api/v1/auth/login').flush('<html>detalhes internos</html>', { status, statusText: 'Erro' });
    await fixture.whenStable();
    const alert = fixture.nativeElement.querySelector('[role="alert"]').textContent;
    expect(alert).toContain(mensagem);
    expect(alert).not.toContain('detalhes internos');
    expect(fixture.nativeElement.querySelector('fieldset').disabled).toBe(false);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('cancels a pending request when leaving the login page', () => {
    preencher();
    enviar();
    const request = http.expectOne('/api/v1/auth/csrf');
    fixture.destroy();
    expect(request.cancelled).toBe(true);
    expect(navigate).not.toHaveBeenCalled();
  });
});
