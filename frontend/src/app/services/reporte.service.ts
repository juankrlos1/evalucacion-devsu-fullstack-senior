import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, ReporteItem } from '../interfaces/models';
import { NotificationService } from './notification.service';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly http = inject(HttpClient);
  private readonly notify = inject(NotificationService);
  private readonly url = `${environment.apiUrl}/reportes`;

  getReporte(fechaInicio: string, fechaFin: string, clienteId: string): Observable<ReporteItem[]> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('cliente', clienteId);

    return this.http.get<ApiResponse<ReporteItem[]>>(this.url, { params }).pipe(
      map(res => res.data ?? []),
      catchError(err => this.handleError(err))
    );
  }

  getReportePdf(fechaInicio: string, fechaFin: string, clienteId: string): Observable<string> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('cliente', clienteId)
      .set('format', 'pdf');

    return this.http.get<ApiResponse<{ pdf: string }>>(this.url, { params }).pipe(
      map(res => res.data?.pdf ?? ''),
      catchError(err => this.handleError(err))
    );
  }

  private handleError(err: any): Observable<never> {
    if (err.status !== 401 && err.status !== 403) {
      const message = err.error?.message || err.error?.errors?.[0] || 'Error al generar el reporte';
      this.notify.error(message);
    }
    return throwError(() => err);
  }
}
