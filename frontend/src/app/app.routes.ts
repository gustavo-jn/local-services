import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Cadastro } from './features/auth/register/register';
import { Login } from './features/auth/login/login';
import { authGuard } from './core/auth.guard';
import { Area } from './features/area/area';
import { Overview } from './features/area/overview';
import { Conta } from './features/conta/conta';
import { Perfil } from './features/perfil/perfil';

export const routes: Routes = [
  { path: '', component: Home, pathMatch: 'full' },
  { path: 'cadastro', component: Cadastro },
  { path: 'login', component: Login, title: 'Entrar | Local Services' },
  {
    path: 'app',
    component: Area,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      { path: '', component: Overview, title: 'Minha área | Local Services' },
      { path: 'conta', component: Conta, title: 'Minha conta | Local Services' },
      {
        path: 'perfil-profissional',
        component: Perfil,
        title: 'Perfil profissional | Local Services',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
