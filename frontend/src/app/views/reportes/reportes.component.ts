import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../services/reporte.service';
import { ClienteService } from '../../services/cliente.service';
import { AuthService } from '../../services/auth.service';
import { ReporteItem, Cliente } from '../../interfaces/models';
import { CurrencyPipe } from '@angular/common';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [FormsModule, CurrencyPipe],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.scss'
})
export class ReportesComponent implements OnInit {
  private readonly service = inject(ReporteService);
  private readonly clienteService = inject(ClienteService);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);

  clientes = signal<Cliente[]>([]);
  reporte = signal<ReporteItem[]>([]);
  loading = signal(false);
  isAdmin = this.auth.isAdmin;

  fechaInicio = '';
  fechaFin = '';
  clienteId = '';

  ngOnInit(): void {
    const today = new Date();
    const monthAgo = new Date(today);
    monthAgo.setMonth(monthAgo.getMonth() - 1);
    this.fechaInicio = monthAgo.toISOString().split('T')[0];
    this.fechaFin = today.toISOString().split('T')[0];

    if (this.auth.isAdmin()) {
      this.clienteService.getAll().subscribe(data => this.clientes.set(data));
    } else {
      // Non-admin: auto-select own customerId
      this.clienteId = this.auth.user()?.customerId ?? '';
    }
  }

  buscar(): void {
    if (!this.fechaInicio || !this.fechaFin) {
      this.notify.warning('Seleccione ambas fechas');
      return;
    }
    if (this.auth.isAdmin() && !this.clienteId) {
      this.notify.warning('Seleccione un cliente');
      return;
    }
    this.loading.set(true);
    this.service.getReporte(this.fechaInicio, this.fechaFin, this.clienteId).subscribe({
      next: data => { this.reporte.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  get totalDebitos(): number {
    return this.reporte().filter(r => r.movement < 0).reduce((sum, r) => sum + r.movement, 0);
  }

  get totalCreditos(): number {
    return this.reporte().filter(r => r.movement > 0).reduce((sum, r) => sum + r.movement, 0);
  }

  descargarPdf(): void {
    const data = this.reporte();
    if (!data.length) { this.notify.warning('No hay datos para exportar'); return; }

    this.service.getReportePdf(this.fechaInicio, this.fechaFin, this.clienteId).subscribe({
      next: (base64) => {
        const byteCharacters = atob(base64);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.codePointAt(i)!;
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: 'application/pdf' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_${this.fechaInicio}_${this.fechaFin}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.notify.success('PDF descargado');
      },
      error: () => {}
    });
  }
}

