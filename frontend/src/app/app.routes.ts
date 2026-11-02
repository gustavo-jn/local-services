import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Cadastro } from './features/auth/register/register';

export const routes: Routes = [
  { path: '', component: Home, pathMatch: 'full' },
  { path: 'cadastro', component: Cadastro },
  { path: '**', redirectTo: '' }
];
