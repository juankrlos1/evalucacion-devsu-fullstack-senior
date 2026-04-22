import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, Movimiento } from '../interfaces/models';
import { NotificationService } from './notification.service';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private readonly http = inject(HttpClient);
  private readonly notify = inject(NotificationService);
  private readonly url = `${environment.apiUrl}/movimientos`;

  getAll(): Observable<Movimiento[]> {
    return this.http.get<ApiResponse<Movimiento[]>>(this.url).pipe(
      map(res => res.data ?? []),
      catchError(err => this.handleError(err))
    );
  }

  create(movimiento: Movimiento): Observable<Movimiento> {
    return this.http.post<ApiResponse<Movimiento>>(this.url, movimiento).pipe(
      map(res => { this.notify.success('Movimiento registrado'); return res.data; }),
      catchError(err => this.handleError(err))
    );
  }

  delete(movementId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.url}/${movementId}`).pipe(
      map(() => { this.notify.success('Movimiento eliminado'); }),
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

