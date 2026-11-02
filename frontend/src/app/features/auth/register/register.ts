import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CadastroApiService } from '../data-access/cadastro-api.service';

type CampoCadastro = 'nome' | 'email' | 'senha';

interface ErroCadastroApi {
  message?: unknown;
  fieldErrors?: unknown;
}

function senhaDentroDoLimite(control: AbstractControl): ValidationErrors | null {
  return new TextEncoder().encode(String(control.value ?? '')).length <= 72 ? null : { senhaMuitoLonga: true };
}

function senhasIguais(control: AbstractControl): ValidationErrors | null {
  const senha = control.get('senha')?.value;
  const confirmacao = control.get('confirmacaoSenha')?.value;
  return !confirmacao || senha === confirmacao ? null : { senhasDiferentes: true };
}

@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../auth-page.css',
})
export class Cadastro {
  private readonly cadastroApi = inject(CadastroApiService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly enviando = signal(false);
  readonly mensagemErro = signal('');
  readonly mensagemSucesso = signal('');
  readonly errosServidor = signal<Partial<Record<CampoCadastro, string>>>({});

  readonly formulario = new FormGroup(
    {
      nome: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)],
      }),
      email: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.email, Validators.maxLength(254)],
      }),
      senha: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(8), senhaDentroDoLimite],
      }),
      confirmacaoSenha: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
    },
    { validators: [senhasIguais] },
  );

  enviar(): void {
    this.limparFeedback();
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.enviando()) {
      return;
    }

    const { nome, email, senha } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.cadastroApi.cadastrar({ nome, email, senha }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.enviando.set(false);
        this.mensagemSucesso.set('Cadastro concluído! Você já pode entrar com seu e-mail e senha.');
        this.formulario.reset();
        void this.router.navigate(['/login']);
      },
      error: erro => this.exibirErro(erro),
    });
  }

  limparFeedback(): void {
    this.mensagemErro.set('');
    this.mensagemSucesso.set('');
    this.errosServidor.set({});
  }

  private exibirErro(erro: unknown): void {
    this.enviando.set(false);

    if (!(erro instanceof HttpErrorResponse)) {
      this.mensagemErro.set('Não foi possível concluir o cadastro. Tente novamente.');
      return;
    }

    if (erro.status === 400) {
      const resposta = erro.error as ErroCadastroApi | null;
      const erros = resposta?.fieldErrors;

      if (erros && typeof erros === 'object') {
        for (const campo of ['nome', 'email', 'senha'] as const) {
          const mensagem = (erros as Record<string, unknown>)[campo];
          if (typeof mensagem === 'string') this.errosServidor.update(atual => ({ ...atual, [campo]: mensagem }));
        }
      }

      if (Object.keys(this.errosServidor()).length === 0) {
        this.mensagemErro.set(typeof resposta?.message === 'string'
          ? resposta.message
          : 'Confira os dados informados e tente novamente.');
      }
      return;
    }

    if (erro.status === 403) {
      this.mensagemErro.set('A sessão de segurança expirou. Tente novamente.');
    } else if (erro.status === 409) {
      this.mensagemErro.set('Este e-mail já está cadastrado. Confira o endereço informado.');
    } else if (erro.status === 0) {
      this.mensagemErro.set('Não foi possível conectar ao servidor. Tente novamente em instantes.');
    } else {
      this.mensagemErro.set('O cadastro não pôde ser concluído agora. Tente novamente mais tarde.');
    }
  }
}
