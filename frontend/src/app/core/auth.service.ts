import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, finalize, of, shareReplay, tap } from 'rxjs';
import { ApiService, Usuario } from './api.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly conta = signal<Usuario | null>(null);
  readonly usuario = this.conta.asReadonly();
  readonly estado = signal<'carregando' | 'autenticado' | 'anonimo' | 'indisponivel'>('carregando');
  private consulta?: Observable<Usuario | null>;

  restaurar(): Observable<Usuario | null> {
    if (this.consulta) return this.consulta;
    this.estado.set('carregando');
    this.consulta = this.http.get<Usuario>('/api/v1/auth/me').pipe(
      tap((usuario) => this.definir(usuario)),
      catchError((erro: unknown) => {
        this.conta.set(null);
        this.estado.set(
          erro instanceof HttpErrorResponse && erro.status === 401 ? 'anonimo' : 'indisponivel',
        );
        return of(null);
      }),
      finalize(() => (this.consulta = undefined)),
      shareReplay({ bufferSize: 1, refCount: false }),
    );
    return this.consulta;
  }

  verificar(): Observable<Usuario | null> {
    return this.estado() === 'autenticado' ? of(this.usuario()) : this.restaurar();
  }
  definir(usuario: Usuario): void {
    this.conta.set(usuario);
    this.estado.set('autenticado');
  }
  limpar(): void {
    this.conta.set(null);
    this.estado.set('anonimo');
  }
  sair() {
    return this.api.alterar<void>('POST', '/api/v1/auth/logout').pipe(tap(() => this.limpar()));
  }
}
