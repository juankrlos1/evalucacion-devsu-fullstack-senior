import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CuentaService } from '../../services/cuenta.service';
import { ClienteService } from '../../services/cliente.service';
import { Cuenta, Cliente } from '../../interfaces/models';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [FormsModule, ConfirmDialogComponent, CurrencyPipe],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.scss'
})
export class CuentasComponent implements OnInit {
  private readonly service = inject(CuentaService);
  private readonly clienteService = inject(ClienteService);

  cuentas = signal<Cuenta[]>([]);
  clientes = signal<Cliente[]>([]);
  searchTerm = signal('');
  showModal = signal(false);
  editing = signal(false);
  deleteTarget = signal<Cuenta | null>(null);
  loading = signal(false);

  form: Cuenta = this.emptyForm();

  filtered = computed(() => {
    const term = this.searchTerm().toLowerCase();
    return this.cuentas().filter(c =>
      c.accountNumber?.includes(term) ||
      c.customerName?.toLowerCase().includes(term) ||
      c.accountType.toLowerCase().includes(term)
    );
  });

  ngOnInit(): void {
    this.clienteService.getAll().subscribe(data => {
      this.clientes.set(data);
      this.load();
    });
  }

  load(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: data => {
        const clienteMap = new Map(this.clientes().map(c => [c.customerId, c.name]));
        const enriched = data.map(c => ({ ...c, customerName: clienteMap.get(c.customerId) || c.customerId }));
        this.cuentas.set(enriched);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openNew(): void { this.form = this.emptyForm(); this.editing.set(false); this.showModal.set(true); }
  openEdit(c: Cuenta): void { this.form = { ...c }; this.editing.set(true); this.showModal.set(true); }

  save(): void {
    if (this.editing()) {
      this.service.patch(this.form.accountNumber, { accountType: this.form.accountType, active: this.form.active, customerId: this.form.customerId })
        .subscribe(() => { this.showModal.set(false); this.load(); });
    } else {
      this.service.create(this.form)
        .subscribe(() => { this.showModal.set(false); this.load(); });
    }
  }

  confirmDelete(c: Cuenta): void { this.deleteTarget.set(c); }

  toggleActive(c: Cuenta): void {
    this.service.patch(c.accountNumber, { active: !c.active }).subscribe(() => this.load());
  }

  doDelete(): void {
    const t = this.deleteTarget();
    if (!t) return;
    this.service.delete(t.accountNumber).subscribe(() => { this.deleteTarget.set(null); this.load(); });
  }

  private emptyForm(): Cuenta {
    return { accountNumber: '', accountType: 'Savings', initialBalance: 0, customerId: '', active: true };
  }
}

