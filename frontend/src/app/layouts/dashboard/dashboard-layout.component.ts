import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../components/header/header.component';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, SidebarComponent],
  template: `
    <app-header />
    <div class="layout-body">
      <app-sidebar />
      <main class="layout-content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .layout-body { display: flex; min-height: calc(100vh - 64px); }
    .layout-content { flex: 1; padding: 28px 36px; overflow-y: auto; background: #f4f5f7; }
  `]
})
export class DashboardLayoutComponent {}

