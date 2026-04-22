import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    <div class="modal-overlay" (click)="onCancel.emit()">
      <div class="modal" (click)="$event.stopPropagation()" style="width:400px">
        <h2>{{ title() }}</h2>
        <p style="color:#5c6370;line-height:1.5">{{ message() }}</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" (click)="onCancel.emit()">Cancelar</button>
          <button class="btn btn-danger" (click)="onConfirm.emit()">Confirmar</button>
        </div>
      </div>
    </div>
  `
})
export class ConfirmDialogComponent {
  title = input('Confirmar');
  message = input('¿Está seguro de realizar esta acción?');
  onConfirm = output<void>();
  onCancel = output<void>();
}

