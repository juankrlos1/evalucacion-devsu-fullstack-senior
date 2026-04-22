import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="sidebar">
      <div class="sidebar-section">Menú principal</div>
      @for (item of visibleMenu(); track item.path) {
        <a [routerLink]="item.path" routerLinkActive="active" class="sidebar-link">
          <span class="link-icon">{{ item.icon }}</span>
          {{ item.label }}
        </a>
      }
    </nav>
  `,
  styles: [`
    .sidebar {
      width: 240px; min-height: calc(100vh - 64px); padding: 20px 0;
      background: #fff; border-right: 1px solid #ebecf0;
    }
    .sidebar-section {
      padding: 8px 24px 12px; font-size: 10px; font-weight: 700;
      text-transform: uppercase; letter-spacing: 1.2px; color: #a0a4b0;
    }
    .sidebar-link {
      display: flex; align-items: center; gap: 12px;
      padding: 12px 24px; color: #5c6370; font-size: 14px; font-weight: 500;
      text-decoration: none; transition: all .15s; border-left: 3px solid transparent;
    }
    .sidebar-link:hover { background: #f4f5f7; color: #1a3067; }
    .sidebar-link.active {
      color: #1a3067; font-weight: 700; background: rgba(26,48,103,.04);
      border-left-color: #ffd100;
    }
    .link-icon { font-size: 18px; width: 24px; text-align: center; }
  `]
})
export class SidebarComponent {
  private readonly auth = inject(AuthService);

  private readonly allMenu = [
    { path: '/clientes', label: 'Clientes', icon: '👤', adminOnly: true },
    { path: '/cuentas', label: 'Cuentas', icon: '💳', adminOnly: false },
    { path: '/movimientos', label: 'Movimientos', icon: '↔', adminOnly: false },
    { path: '/reportes', label: 'Reportes', icon: '📊', adminOnly: false },
  ];

  readonly visibleMenu = computed(() =>
    this.auth.isAdmin()
      ? this.allMenu
      : this.allMenu.filter(item => !item.adminOnly)
  );
}
