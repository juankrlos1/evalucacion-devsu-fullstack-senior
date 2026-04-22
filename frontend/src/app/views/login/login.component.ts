import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-brand">
          <span class="brand-icon">▶</span>
          <span class="brand-text">BANCO<br><small>PICHINCHA</small></span>
        </div>
        <h2 class="login-title">Iniciar Sesión</h2>

        @if (errorMsg()) {
          <div class="login-error">{{ errorMsg() }}</div>
        }

        <form (ngSubmit)="onSubmit()" class="login-form">
          <div class="form-group">
            <label for="identification">Identificación</label>
            <input id="identification" type="text" [(ngModel)]="identification" name="identification"
                   placeholder="Ingrese su identificación" required autocomplete="username" />
          </div>
          <div class="form-group">
            <label for="password">Contraseña</label>
            <input id="password" type="password" [(ngModel)]="password" name="password"
                   placeholder="Ingrese su contraseña" required autocomplete="current-password" />
          </div>
          <button type="submit" class="login-btn" [disabled]="loading()">
            {{ loading() ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      display: flex; align-items: center; justify-content: center;
      min-height: 100vh; background: linear-gradient(135deg, #1a3067 0%, #0d1b3e 100%);
    }
    .login-card {
      background: #fff; border-radius: 12px; padding: 48px 40px; width: 400px;
      box-shadow: 0 20px 60px rgba(0,0,0,.3);
    }
    .login-brand {
      display: flex; align-items: center; gap: 10px; justify-content: center; margin-bottom: 32px;
    }
    .brand-icon {
      display: flex; align-items: center; justify-content: center;
      width: 36px; height: 36px; background: #ffd100; color: #1a3067;
      font-size: 16px; font-weight: 900; border-radius: 6px;
    }
    .brand-text {
      font-size: 16px; font-weight: 800; letter-spacing: 1.5px; line-height: 1.1; color: #1a3067;
      small { font-size: 16px; font-weight: 800; }
    }
    .login-title {
      text-align: center; font-size: 20px; font-weight: 600; color: #333; margin-bottom: 24px;
    }
    .login-error {
      background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
      padding: 10px 14px; border-radius: 8px; font-size: 13px; margin-bottom: 16px; text-align: center;
    }
    .form-group {
      margin-bottom: 20px;
      label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
      input {
        width: 100%; padding: 12px 14px; border: 1px solid #d1d5db; border-radius: 8px;
        font-size: 14px; transition: border-color .15s; box-sizing: border-box;
        &:focus { outline: none; border-color: #1a3067; box-shadow: 0 0 0 3px rgba(26,48,103,.1); }
      }
    }
    .login-btn {
      width: 100%; padding: 14px; background: #1a3067; color: #fff; border: none;
      border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; transition: background .15s;
      &:hover:not(:disabled) { background: #243d7a; }
      &:disabled { opacity: .6; cursor: not-allowed; }
    }
  `]
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  identification = '';
  password = '';
  loading = signal(false);
  errorMsg = signal('');

  onSubmit(): void {
    if (!this.identification || !this.password) {
      this.errorMsg.set('Complete todos los campos');
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    this.auth.login({ identification: this.identification, password: this.password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err.message || 'Error de autenticación');
      }
    });
  }
}
