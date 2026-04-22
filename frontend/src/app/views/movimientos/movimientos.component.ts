import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MovimientoService } from '../../services/movimiento.service';
import { CuentaService } from '../../services/cuenta.service';
import { ClienteService } from '../../services/cliente.service';
import { Movimiento, Cuenta } from '../../interfaces/models';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [FormsModule, ConfirmDialogComponent, CurrencyPipe, DatePipe],
  templateUrl: './movimientos.component.html',
  styleUrl: './movimientos.component.scss'
})
export class MovimientosComponent implements OnInit {
  private readonly service = inject(MovimientoService);
  private readonly cuentaService = inject(CuentaService);
  private readonly clienteService = inject(ClienteService);

  movimientos = signal<Movimiento[]>([]);
  cuentas = signal<Cuenta[]>([]);
  searchTerm = signal('');
  showModal = signal(false);
  deleteTarget = signal<Movimiento | null>(null);
  loading = signal(false);

  form: Movimiento = this.emptyForm();

  filtered = computed(() => {
    const term = this.searchTerm().toLowerCase();
    return this.movimientos().filter(m =>
      m.accountNumber?.includes(term) ||
      m.movementType?.toLowerCase().includes(term)
    );
  });

  ngOnInit(): void {
    this.load();
    this.clienteService.getAll().subscribe(clientes => {
      const clienteMap = new Map(clientes.map(c => [c.customerId, c.name]));
      this.cuentaService.getAll().subscribe(data => {
        const enriched = data.map(c => ({ ...c, customerName: clienteMap.get(c.customerId) || c.customerId }));
        this.cuentas.set(enriched);
      });
    });
  }

  load(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: data => { this.movimientos.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openNew(): void { this.form = this.emptyForm(); this.showModal.set(true); }

  save(): void {
    const payload: Movimiento = {
      accountNumber: this.form.accountNumber,
      value: this.form.movementType === 'Withdrawal' ? -Math.abs(this.form.value) : Math.abs(this.form.value)
    };
    this.service.create(payload).subscribe(() => {
      this.showModal.set(false); this.load();
    });
  }

  confirmDelete(m: Movimiento): void { this.deleteTarget.set(m); }
  doDelete(): void {
    const t = this.deleteTarget();
    if (!t) return;
    this.service.delete(t.movementId!).subscribe(() => { this.deleteTarget.set(null); this.load(); });
  }

  private emptyForm(): Movimiento {
    return { movementType: 'Deposit', value: 0, accountNumber: '' };
  }
}

