import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  template: `
    <header class="header">
      <div class="header-brand">
        <span class="brand-icon">▶</span>
        <span class="brand-text">BANCO<br><small>PICHINCHA</small></span>
      </div>
      <nav class="header-nav">
        @if (auth.isAuthenticated()) {
          <span class="header-role" [class.admin]="auth.isAdmin()">{{ auth.isAdmin() ? 'Admin' : 'Usuario' }}</span>
          <span class="header-user">{{ auth.userName() }}</span>
          <button class="logout-btn" (click)="auth.logout()">Salir</button>
        }
      </nav>
    </header>
  `,
  styles: [`
    .header {
      height: 64px; display: flex; align-items: center; justify-content: space-between;
      padding: 0 32px; background: #1a3067; color: #fff;
    }
    .header-brand {
      display: flex; align-items: center; gap: 10px;
    }
    .brand-icon {
      display: flex; align-items: center; justify-content: center;
      width: 32px; height: 32px; background: #ffd100; color: #1a3067;
      font-size: 14px; font-weight: 900; border-radius: 4px;
    }
    .brand-text {
      font-size: 14px; font-weight: 800; letter-spacing: 1.5px; line-height: 1.1;
      small { font-size: 14px; font-weight: 800; }
    }
    .header-nav {
      display: flex; align-items: center; gap: 14px;
    }
    .header-role {
      font-size: 11px; font-weight: 700; text-transform: uppercase;
      padding: 3px 10px; border-radius: 12px; background: rgba(255,255,255,.15); color: rgba(255,255,255,.8);
      letter-spacing: .5px;
    }
    .header-role.admin { background: #ffd100; color: #1a3067; }
    .header-user {
      font-size: 13px; color: rgba(255,255,255,.85); font-weight: 500;
    }
    .logout-btn {
      background: rgba(255,255,255,.12); border: 1px solid rgba(255,255,255,.2);
      color: #fff; padding: 6px 16px; border-radius: 6px; font-size: 12px;
      font-weight: 600; cursor: pointer; transition: background .15s;
    }
    .logout-btn:hover { background: rgba(255,255,255,.22); }
  `]
})
export class HeaderComponent {
  readonly auth = inject(AuthService);
}
