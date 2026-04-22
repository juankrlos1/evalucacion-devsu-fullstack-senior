import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../interfaces/models';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [FormsModule, ConfirmDialogComponent],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.scss'
})
export class ClientesComponent implements OnInit {
  private readonly service = inject(ClienteService);

  clientes = signal<Cliente[]>([]);
  searchTerm = signal('');
  showModal = signal(false);
  editing = signal(false);
  deleteTarget = signal<Cliente | null>(null);
  loading = signal(false);

  form: Cliente = this.emptyForm();

  filtered = computed(() => {
    const term = this.searchTerm().toLowerCase();
    return this.clientes().filter(c =>
      c.name.toLowerCase().includes(term) ||
      c.identification.includes(term) ||
      c.customerId?.toLowerCase().includes(term)
    );
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: data => { this.clientes.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openNew(): void {
    this.form = this.emptyForm();
    this.editing.set(false);
    this.showModal.set(true);
  }

  openEdit(c: Cliente): void {
    this.form = { ...c };
    this.editing.set(true);
    this.showModal.set(true);
  }

  save(): void {
    if (this.editing()) {
      this.service.patch(this.form.customerId!, this.form).subscribe(() => {
        this.showModal.set(false); this.load();
      });
    } else {
      this.service.create(this.form).subscribe(() => {
        this.showModal.set(false); this.load();
      });
    }
  }

  toggleActive(c: Cliente): void {
    this.service.patch(c.customerId, { active: !c.active }).subscribe(() => this.load());
  }

  confirmDelete(c: Cliente): void { this.deleteTarget.set(c); }

  doDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.service.delete(target.customerId!).subscribe(() => {
      this.deleteTarget.set(null); this.load();
    });
  }

  private emptyForm(): Cliente {
    return { customerId: '', name: '', gender: 'Male', age: 18, identification: '', address: '', phone: '', password: '', active: true };
  }
}

