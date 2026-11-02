import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService, erroApi } from '../../core/api.service';

export interface PerfilResponse {
  id: string;
  nome: string;
  descricao: string;
  aceitaNovosServicos: boolean;
  publicado: boolean;
  agenda: { id: string; fusoReferencia: string };
}
@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule],
  templateUrl: './perfil.html',
  styleUrl: '../area/page.css',
})
export class Perfil {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  readonly perfil = signal<PerfilResponse | null>(null);
  readonly carregando = signal(true);
  readonly enviando = signal(false);
  readonly erroCarga = signal('');
  readonly erro = signal('');
  readonly sucesso = signal('');
  readonly formulario = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)],
    }),
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(10), Validators.maxLength(3000)],
    }),
    aceitaNovosServicos: new FormControl(true, { nonNullable: true }),
  });
  constructor() {
    this.carregar();
  }
  carregar(): void {
    this.carregando.set(true);
    this.erroCarga.set('');
    this.http
      .get<PerfilResponse>('/api/v1/me/perfil-profissional')
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregando.set(false)),
      )
      .subscribe({
        next: (perfil) => {
          this.perfil.set(perfil);
          this.formulario.patchValue(perfil);
        },
        error: (erro) => {
          if (erro instanceof HttpErrorResponse && erro.status === 404) this.perfil.set(null);
          else this.erroCarga.set(erroApi(erro));
        },
      });
  }
  salvar(): void {
    if (this.enviando()) return;
    this.formulario.markAllAsTouched();
    this.erro.set('');
    this.sucesso.set('');
    if (this.formulario.invalid) return;
    this.enviando.set(true);
    this.api
      .alterar<PerfilResponse>(
        this.perfil() ? 'PATCH' : 'POST',
        '/api/v1/me/perfil-profissional',
        this.formulario.getRawValue(),
      )
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.enviando.set(false)),
      )
      .subscribe({
        next: (perfil) => {
          const novo = !this.perfil();
          this.perfil.set(perfil);
          this.formulario.patchValue(perfil);
          this.sucesso.set(
            novo ? 'Perfil profissional criado com sucesso.' : 'Perfil atualizado com sucesso.',
          );
        },
        error: (erro) => this.erro.set(erroApi(erro)),
      });
  }
}
