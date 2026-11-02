import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return next(request).pipe(
    catchError((erro: unknown) => {
      if (
        erro instanceof HttpErrorResponse &&
        erro.status === 401 &&
        request.url.startsWith('/api/v1/me')
      ) {
        auth.limpar();
        void router.navigate(['/login'], { queryParams: { motivo: 'sessao' } });
      }
      return throwError(() => erro);
    }),
  );
};
