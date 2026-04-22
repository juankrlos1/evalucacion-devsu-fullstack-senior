import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NotificationComponent } from './components/notification/notification.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NotificationComponent],
  template: `
    <router-outlet />
    <app-notification />
  `,
  styles: [`:host { display: block; height: 100%; }`]
})
export class App {}
