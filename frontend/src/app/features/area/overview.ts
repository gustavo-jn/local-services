import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
@Component({
  selector: 'app-overview',
  imports: [RouterLink],
  styleUrl: './page.css',
  template: `<span class="eyebrow">MINHA ÁREA</span>
    <h1>Olá, {{ auth.usuario()?.nome }}.</h1>
    <p class="intro">
      Bom ter você por aqui. Cuide dos seus dados e, se você oferece serviços, dê o próximo passo
      para apresentar seu trabalho.
    </p>
    <div class="cards">
      <section class="card">
        <span class="card-number" aria-hidden="true">01</span>
        <h2>Seus dados, em dia</h2>
        <p>Atualize seu nome, e-mail ou senha em um só lugar.</p>
        <a class="button secondary" routerLink="/app/conta">Minha conta</a>
      </section>
      <section class="card">
        <span class="card-number" aria-hidden="true">02</span>
        <h2>Seu trabalho começa aqui</h2>
        <p>
          Crie ou edite seu perfil profissional. Você pode oferecer serviços e contratar com a mesma
          conta.
        </p>
        <a class="button" routerLink="/app/perfil-profissional">Meu perfil profissional</a>
      </section>
    </div>`,
})
export class Overview {
  readonly auth = inject(AuthService);
}
