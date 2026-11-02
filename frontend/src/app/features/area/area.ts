import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { erroApi } from '../../core/api.service';

@Component({
  selector: 'app-area',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './area.html',
  styleUrl: './area.css',
})
export class Area {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly saindo = signal(false);
  readonly erro = signal('');
  sair(): void {
    if (this.saindo()) return;
    this.saindo.set(true);
    this.erro.set('');
    this.auth.sair().subscribe({
      next: () => {
        this.saindo.set(false);
        void this.router.navigateByUrl('/');
      },
      error: (erro) => {
        this.saindo.set(false);
        this.erro.set(erroApi(erro));
      },
    });
  }
}
