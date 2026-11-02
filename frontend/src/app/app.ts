import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from './core/auth.service';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly auth = inject(AuthService);
  ngOnInit(): void { this.auth.restaurar().subscribe(); }
}
