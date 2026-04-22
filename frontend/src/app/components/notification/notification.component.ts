import { Component, inject } from '@angular/core';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  template: `
    <div class="toast-container">
      @for (n of notificationService.notifications(); track n.id) {
        <div class="toast" [class]="n.type" (click)="notificationService.dismiss(n.id)">
          <span class="toast-icon">{{ n.type === 'success' ? '✓' : n.type === 'error' ? '✕' : '!' }}</span>
          <span>{{ n.message }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container { position: fixed; top: 20px; right: 20px; z-index: 2000; display: flex; flex-direction: column; gap: 10px; }
    .toast {
      display: flex; align-items: center; gap: 10px;
      padding: 14px 20px; border-radius: 8px; color: #fff; font-size: 13px; font-weight: 500;
      min-width: 300px; max-width: 440px; cursor: pointer;
      box-shadow: 0 6px 20px rgba(0,0,0,.2); animation: slideIn .3s cubic-bezier(.4,0,.2,1);
    }
    .toast-icon { font-weight: 800; font-size: 16px; }
    .success { background: #2e7d32; }
    .error { background: #c62828; }
    .warning { background: #1a3067; color: #ffd100; }
    .info { background: #1a3067; }
    @keyframes slideIn { from { transform: translateX(120%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
  `]
})
export class NotificationComponent {
  readonly notificationService = inject(NotificationService);
}
