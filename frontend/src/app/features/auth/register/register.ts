import { Component } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';

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
  enviado = false;

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
    this.enviado = false;
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid) {
      return;
    }

    this.enviado = true;
  }
}
