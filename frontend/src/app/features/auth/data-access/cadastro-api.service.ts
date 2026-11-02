import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap } from 'rxjs';

export interface CadastroUsuarioRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface CadastroUsuarioResponse {
  id: string;
  nome: string;
  email: string;
}

interface CsrfResponse {
  headerName: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class CadastroApiService {
  private readonly http = inject(HttpClient);

  cadastrar(dados: CadastroUsuarioRequest): Observable<CadastroUsuarioResponse> {
    return this.http.get<CsrfResponse>('/api/v1/auth/csrf').pipe(
      switchMap(({ headerName, token }) =>
        this.http.post<CadastroUsuarioResponse>(
          '/api/v1/auth/register',
          dados,
          { headers: new HttpHeaders().set(headerName, token) },
        ),
      ),
    );
  }
}
