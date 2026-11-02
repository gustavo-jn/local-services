import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  const usuario = { id: 'id', nome: 'Exemplo', email: 'exemplo@example.com' };
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('restores identity once for simultaneous callers without storing the token', () => {
    auth.restaurar().subscribe();
    auth.restaurar().subscribe();
    http.expectOne('/api/v1/auth/me').flush(usuario);
    expect(auth.usuario()).toEqual(usuario);
    expect(auth.estado()).toBe('autenticado');
  });
  it('distinguishes a signed-out session from server unavailability', () => {
    auth.restaurar().subscribe();
    http.expectOne('/api/v1/auth/me').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.estado()).toBe('anonimo');
    auth.restaurar().subscribe();
    http.expectOne('/api/v1/auth/me').error(new ProgressEvent('error'));
    expect(auth.estado()).toBe('indisponivel');
  });
  it('only clears identity after successful CSRF-protected logout', () => {
    auth.definir(usuario);
    auth.sair().subscribe({ error: () => {} });
    http.expectOne('/api/v1/auth/csrf').flush({ headerName: 'X-CSRF-TOKEN', token: 'primeiro' });
    const primeiro = http.expectOne('/api/v1/auth/logout');
    expect(primeiro.request.headers.get('X-CSRF-TOKEN')).toBe('primeiro');
    primeiro.flush({}, { status: 503, statusText: 'Unavailable' });
    expect(auth.usuario()).toEqual(usuario);
    auth.sair().subscribe();
    http.expectOne('/api/v1/auth/csrf').flush({ headerName: 'X-CSRF-TOKEN', token: 'novo' });
    http.expectOne('/api/v1/auth/logout').flush(null, { status: 204, statusText: 'No Content' });
    expect(auth.usuario()).toBeNull();
    expect(auth.estado()).toBe('anonimo');
  });
});
