import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AbstractControl, ValidationErrors } from '@angular/forms';
import { switchMap } from 'rxjs';

export interface Usuario {
  id: string;
  nome: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  alterar<T>(method: 'POST' | 'PATCH', url: string, body: unknown = {}) {
    return this.http.get<{ headerName: string; token: string }>('/api/v1/auth/csrf').pipe(
      switchMap((csrf) =>
        this.http.request<T>(method, url, {
          body,
          headers: new HttpHeaders().set(csrf.headerName, csrf.token),
        }),
      ),
    );
  }
}

export function erroApi(erro: unknown): string {
  if (!(erro instanceof HttpErrorResponse)) return 'Não foi possível concluir. Tente novamente.';
  if (erro.status === 0)
    return 'Não foi possível conectar. Verifique sua conexão e tente novamente.';
  if (erro.status === 401) return 'Sua sessão terminou. Entre novamente para continuar.';
  if (erro.status === 403) return 'A sessão de segurança expirou. Tente novamente.';
  if (erro.status >= 500)
    return 'O serviço está indisponível no momento. Tente novamente mais tarde.';
  const fields = erro.error?.fieldErrors;
  if (fields && typeof fields === 'object') {
    const mensagens = Object.values(fields).filter(
      (value): value is string => typeof value === 'string',
    );
    if (mensagens.length) return mensagens.join('. ');
  }
  return typeof erro.error?.message === 'string'
    ? erro.error.message
    : 'Confira os dados e tente novamente.';
}

export function limiteSenha(control: AbstractControl): ValidationErrors | null {
  return new TextEncoder().encode(String(control.value ?? '')).length <= 72
    ? null
    : { limiteSenha: true };
}
