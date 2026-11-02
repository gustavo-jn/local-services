import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap } from 'rxjs';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  id: string;
  nome: string;
  email: string;
}

interface CsrfResponse {
  headerName: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class LoginApiService {
  private readonly http = inject(HttpClient);

  entrar(dados: LoginRequest): Observable<LoginResponse> {
    // Cada tentativa usa o CSRF da sessão atual; o navegador gerencia os cookies.
    return this.http.get<CsrfResponse>('/api/v1/auth/csrf').pipe(
      switchMap(({ headerName, token }) =>
        this.http.post<LoginResponse>(
          '/api/v1/auth/login',
          { email: dados.email.trim().toLowerCase(), senha: dados.senha },
          { headers: new HttpHeaders().set(headerName, token) },
        ),
      ),
    );
  }
}
