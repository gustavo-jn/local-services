import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
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
  styleUrl: './register.css',
})
export class Cadastro {
  private readonly cadastroApi = inject(CadastroApiService);

  enviando = false;
  mensagemErro = '';
  mensagemSucesso = '';
  errosServidor: Partial<Record<CampoCadastro, string>> = {};

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

    if (this.formulario.invalid || this.enviando) {
      return;
    }

    const { nome, email, senha } = this.formulario.getRawValue();
    this.enviando = true;
    this.cadastroApi.cadastrar({ nome, email, senha }).subscribe({
      next: () => {
        this.enviando = false;
        this.mensagemSucesso = 'Cadastro concluído. Você poderá acessar sua conta quando o login estiver disponível.';
        this.formulario.reset();
      },
      error: erro => this.exibirErro(erro),
    });
  }

  limparFeedback(): void {
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.errosServidor = {};
  }

  private exibirErro(erro: unknown): void {
    this.enviando = false;

    if (!(erro instanceof HttpErrorResponse)) {
      this.mensagemErro = 'Não foi possível concluir o cadastro. Tente novamente.';
      return;
    }

    if (erro.status === 400) {
      const resposta = erro.error as ErroCadastroApi | null;
      const erros = resposta?.fieldErrors;

      if (erros && typeof erros === 'object') {
        for (const campo of ['nome', 'email', 'senha'] as const) {
          const mensagem = (erros as Record<string, unknown>)[campo];
          if (typeof mensagem === 'string') this.errosServidor[campo] = mensagem;
        }
      }

      if (Object.keys(this.errosServidor).length === 0) {
        this.mensagemErro = typeof resposta?.message === 'string'
          ? resposta.message
          : 'Confira os dados informados e tente novamente.';
      }
      return;
    }

    if (erro.status === 403) {
      this.mensagemErro = 'A sessão de segurança expirou. Atualize a página e tente novamente.';
    } else if (erro.status === 409) {
      this.mensagemErro = 'Este e-mail já está cadastrado. Confira o endereço informado.';
    } else if (erro.status === 0) {
      this.mensagemErro = 'Não foi possível conectar ao servidor. Verifique se a API está em execução.';
    } else {
      this.mensagemErro = 'O cadastro não pôde ser concluído agora. Tente novamente mais tarde.';
    }
  }
}
