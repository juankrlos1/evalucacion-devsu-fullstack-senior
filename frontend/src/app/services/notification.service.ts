import { Injectable, signal } from '@angular/core';

export interface Notification {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private counter = 0;
  readonly notifications = signal<Notification[]>([]);

  show(message: string, type: Notification['type'] = 'info', duration = 4000): void {
    const id = ++this.counter;
    this.notifications.update(list => [...list, { id, message, type }]);
    setTimeout(() => this.dismiss(id), duration);
  }

  success(message: string): void { this.show(message, 'success'); }
  error(message: string): void { this.show(message, 'error', 6000); }
  warning(message: string): void { this.show(message, 'warning'); }

  dismiss(id: number): void {
    this.notifications.update(list => list.filter(n => n.id !== id));
  }
}

