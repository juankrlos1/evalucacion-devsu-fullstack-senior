import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, Cuenta } from '../interfaces/models';
import { NotificationService } from './notification.service';

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private readonly http = inject(HttpClient);
  private readonly notify = inject(NotificationService);
  private readonly url = `${environment.apiUrl}/cuentas`;

  getAll(): Observable<Cuenta[]> {
    return this.http.get<ApiResponse<Cuenta[]>>(this.url).pipe(
      map(res => res.data ?? []),
      catchError(err => this.handleError(err))
    );
  }

  getById(accountNumber: string): Observable<Cuenta> {
    return this.http.get<ApiResponse<Cuenta>>(`${this.url}/${accountNumber}`).pipe(
      map(res => res.data),
      catchError(err => this.handleError(err))
    );
  }

  create(cuenta: Cuenta): Observable<Cuenta> {
    return this.http.post<ApiResponse<Cuenta>>(this.url, cuenta).pipe(
      map(res => { this.notify.success('Cuenta creada exitosamente'); return res.data; }),
      catchError(err => this.handleError(err))
    );
  }

  update(accountNumber: string, cuenta: Partial<Cuenta>): Observable<Cuenta> {
    return this.http.put<ApiResponse<Cuenta>>(`${this.url}/${accountNumber}`, cuenta).pipe(
      map(res => { this.notify.success('Cuenta actualizada'); return res.data; }),
      catchError(err => this.handleError(err))
    );
  }

  patch(accountNumber: string, data: Partial<Cuenta>): Observable<Cuenta> {
    return this.http.patch<ApiResponse<Cuenta>>(`${this.url}/${accountNumber}`, data).pipe(
      map(res => { this.notify.success('Cuenta actualizada'); return res.data; }),
      catchError(err => this.handleError(err))
    );
  }

  delete(accountNumber: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.url}/${accountNumber}`).pipe(
      map(() => { this.notify.success('Cuenta eliminada'); }),
      catchError(err => this.handleError(err))
    );
  }

  private handleError(err: any): Observable<never> {
    if (err.status !== 401 && err.status !== 403) {
      const message = err.error?.message || err.error?.errors?.[0] || 'Error de conexión con el servidor';
      this.notify.error(message);
    }
    return throwError(() => err);
  }
}

