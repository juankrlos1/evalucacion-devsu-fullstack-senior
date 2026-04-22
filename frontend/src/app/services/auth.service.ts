import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, map, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, AuthRequest, AuthResponse } from '../interfaces/models';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly url = `${environment.apiUrl}/auth`;

  private readonly _user = signal<AuthResponse | null>(this.loadUser());

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => !!this._user());
  readonly isAdmin = computed(() => this._user()?.role === 'ADMIN');
  readonly userName = computed(() => this._user()?.name ?? '');

  login(request: AuthRequest): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.url}/login`, request).pipe(
      map(res => res.data),
      tap(data => {
        localStorage.setItem(TOKEN_KEY, data.token);
        localStorage.setItem(USER_KEY, JSON.stringify(data));
        this._user.set(data);
      }),
      catchError(err => {
        const message = err.error?.message || 'Error de autenticación';
        return throwError(() => new Error(message));
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._user.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private loadUser(): AuthResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }
}
