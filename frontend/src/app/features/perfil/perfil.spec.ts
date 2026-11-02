import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Perfil } from './perfil';

describe('Perfil', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Perfil],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('starts creation only on 404 and switches to editing after the successful POST', () => {
    const fixture = TestBed.createComponent(Perfil);
    const perfil = fixture.componentInstance;
    http
      .expectOne('/api/v1/me/perfil-profissional')
      .flush({}, { status: 404, statusText: 'Not Found' });
    perfil.formulario.setValue({
      nome: 'Profissional',
      descricao: 'Descrição do trabalho oferecido.',
      aceitaNovosServicos: true,
    });
    perfil.salvar();
    perfil.salvar();
    http.expectOne('/api/v1/auth/csrf').flush({ headerName: 'X-CSRF-TOKEN', token: 'token' });
    const post = http.expectOne('/api/v1/me/perfil-profissional');
    expect(post.request.method).toBe('POST');
    post.flush({
      id: 'perfil',
      ...perfil.formulario.getRawValue(),
      publicado: false,
      agenda: { id: 'agenda', fusoReferencia: 'America/Sao_Paulo' },
    });
    expect(perfil.perfil()?.id).toBe('perfil');
    perfil.salvar();
    http.expectOne('/api/v1/auth/csrf').flush({ headerName: 'X-CSRF-TOKEN', token: 'novo' });
    const patch = http.expectOne('/api/v1/me/perfil-profissional');
    expect(patch.request.method).toBe('PATCH');
    patch.flush(perfil.perfil());
    fixture.destroy();
  });
  it('does not mistake a network failure for an absent profile', () => {
    const fixture = TestBed.createComponent(Perfil);
    http.expectOne('/api/v1/me/perfil-profissional').error(new ProgressEvent('error'));
    expect(fixture.componentInstance.erroCarga()).toContain('Não foi possível conectar');
    fixture.destroy();
  });
});
