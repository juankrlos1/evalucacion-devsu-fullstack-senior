import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, Cliente } from '../interfaces/models';
import { NotificationService } from './notification.service';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly notify = inject(NotificationService);
  private readonly url = `${environment.apiUrl}/clientes`;

  getAll(): Observable<Cliente[]> {
    return this.http.get<ApiResponse<Cliente[]>>(this.url).pipe(
      map(res => res.data ?? []),
      catchError(err => this.handleError(err))
    );
  }

  getById(id: string): Observable<Cliente> {
    return this.http.get<ApiResponse<Cliente>>(`${this.url}/${id}`).pipe(
      map(res => res.data),
      catchError(err => this.handleError(err))
    );
  }

  create(cliente: Cliente): Observable<Cliente> {
    return this.http.post<ApiResponse<Cliente>>(this.url, cliente).pipe(
      map(res => { this.notify.success('Cliente creado exitosamente'); return res.data; }),
      catchError(err => this.handleError(err))
    );
  }

  update(id: string, cliente: Partial<Cliente>): Observable<Cliente> {
    return this.http.put<ApiResponse<Cliente>>(`${this.url}/${id}`, cliente).pipe(
      map(res => { this.notify.success('Cliente actualizado'); return res.data; }),
      catchError(err => this.handleError(err))
    );
  }

  patch(id: string, data: Partial<Cliente>): Observable<Cliente> {
    return this.http.patch<ApiResponse<Cliente>>(`${this.url}/${id}`, data).pipe(
      map(res => { this.notify.success('Cliente actualizado'); return res.data; }),
      catchError(err => this.handleError(err))
    );
  }

  delete(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.url}/${id}`).pipe(
      map(() => { this.notify.success('Cliente eliminado'); }),
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

