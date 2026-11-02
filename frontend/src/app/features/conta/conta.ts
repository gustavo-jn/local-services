import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService, Usuario, erroApi, limiteSenha } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-conta',
  imports: [ReactiveFormsModule],
  templateUrl: './conta.html',
  styleUrl: '../area/page.css',
})
export class Conta {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  readonly carregando = signal(true);
  readonly erroCarga = signal('');
  readonly enviando = signal<string | null>(null);
  readonly erro = signal('');
  readonly sucesso = signal('');
  readonly nome = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)],
    }),
  });
  readonly email = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(254)],
    }),
    senhaAtual: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, limiteSenha],
    }),
  });
  readonly senha = new FormGroup(
    {
      senhaAtual: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, limiteSenha],
      }),
      novaSenha: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(8), limiteSenha],
      }),
      confirmacao: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    },
    {
      validators: (control) =>
        control.get('novaSenha')?.value === control.get('confirmacao')?.value
          ? null
          : { senhasDiferentes: true },
    },
  );
  constructor() {
    this.carregar();
  }
  carregar(): void {
    this.carregando.set(true);
    this.erroCarga.set('');
    this.http
      .get<Usuario>('/api/v1/me')
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregando.set(false)),
      )
      .subscribe({
        next: (usuario) => {
          this.auth.definir(usuario);
          this.nome.patchValue({ nome: usuario.nome });
          this.email.patchValue({ email: usuario.email });
        },
        error: (erro) => this.erroCarga.set(erroApi(erro)),
      });
  }
  salvar(tipo: 'nome' | 'email' | 'senha'): void {
    if (this.enviando()) return;
    const form = this[tipo];
    form.markAllAsTouched();
    this.erro.set('');
    this.sucesso.set('');
    if (form.invalid) return;
    this.enviando.set(tipo);
    const body =
      tipo === 'senha'
        ? {
            senhaAtual: this.senha.controls.senhaAtual.value,
            novaSenha: this.senha.controls.novaSenha.value,
          }
        : form.getRawValue();
    this.api
      .alterar<Usuario | null>('PATCH', tipo === 'senha' ? '/api/v1/me/senha' : '/api/v1/me', body)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.enviando.set(null)),
      )
      .subscribe({
        next: (usuario) => {
          if (usuario) {
            this.auth.definir(usuario);
            this.nome.patchValue({ nome: usuario.nome });
            this.email.patchValue({ email: usuario.email });
          }
          this.email.controls.senhaAtual.reset();
          this.senha.reset();
          this.sucesso.set(
            tipo === 'senha'
              ? 'Senha alterada. Use a nova senha no próximo acesso.'
              : 'Dados atualizados com sucesso.',
          );
        },
        error: (erro) => this.erro.set(erroApi(erro)),
      });
  }
}
