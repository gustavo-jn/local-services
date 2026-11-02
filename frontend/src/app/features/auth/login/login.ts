import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, ElementRef, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth.service';
import { finalize } from 'rxjs';
import { LoginApiService } from '../data-access/login-api.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['../auth-page.css', './login.css'],
})
export class Login {
  private readonly loginApi = inject(LoginApiService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  readonly aviso = this.route.snapshot.queryParamMap.get('motivo') === 'sessao'
    ? 'Entre na sua conta para continuar.'
    : this.route.snapshot.queryParamMap.get('motivo') === 'conexao'
      ? 'Não foi possível verificar sua conta. Tente entrar novamente.' : '';
  private readonly destroyRef = inject(DestroyRef);
  private readonly formElement = viewChild<ElementRef<HTMLFormElement>>('loginForm');

  readonly enviando = signal(false);
  readonly mensagemErro = signal('');
  readonly senhaVisivel = signal(false);

  readonly formulario = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(254)],
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  enviar(): void {
    if (this.enviando()) return;

    const email = this.formulario.controls.email;
    email.setValue(email.value.trim());
    this.limparFeedback();
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid) {
      this.formElement()?.nativeElement.querySelector<HTMLInputElement>(email.invalid ? '#login-email' : '#login-senha')?.focus();
      return;
    }

    this.enviando.set(true);
    this.loginApi.entrar(this.formulario.getRawValue()).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.enviando.set(false)),
    ).subscribe({
      next: usuario => {
        this.auth.definir(usuario);
        this.formulario.controls.senha.reset();
        this.senhaVisivel.set(false);
        void this.router.navigateByUrl('/app');
      },
      error: (erro: unknown) => this.exibirErro(erro),
    });
  }

  limparFeedback(): void {
    this.mensagemErro.set('');
  }

  private exibirErro(erro: unknown): void {
    if (!(erro instanceof HttpErrorResponse)) {
      this.mensagemErro.set('Não foi possível entrar. Tente novamente.');
      return;
    }

    switch (erro.status) {
      case 400:
        this.mensagemErro.set('Confira o e-mail e a senha informados e tente novamente.');
        break;
      case 401:
        this.mensagemErro.set('E-mail ou senha incorretos. Confira seus dados e tente novamente.');
        break;
      case 403:
        this.mensagemErro.set('Sua sessão de segurança expirou. Clique em Entrar para tentar novamente.');
        break;
      case 0:
        this.mensagemErro.set('Não foi possível conectar ao servidor. Tente novamente em instantes.');
        break;
      default:
        this.mensagemErro.set('Não foi possível entrar agora. Tente novamente mais tarde.');
    }
  }
}
