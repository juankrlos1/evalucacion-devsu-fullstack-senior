import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const notify = inject(NotificationService);

  const token = auth.getToken();
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req).pipe(
    catchError(err => {
      if (err.status === 401) {
        auth.logout();
      } else if (err.status === 403) {
        notify.warning('No tiene permisos para realizar esta acción');
      } else if (err.status === 503) {
        notify.error('Servicio no disponible. Intente nuevamente.');
      }
      return throwError(() => err);
    })
  );
};
