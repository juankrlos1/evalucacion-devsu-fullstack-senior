import { Routes } from '@angular/router';
import { DashboardLayoutComponent } from './layouts/dashboard/dashboard-layout.component';
import { authGuard, adminGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./views/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'cuentas', pathMatch: 'full' },
      { path: 'clientes', loadComponent: () => import('./views/clientes/clientes.component').then(m => m.ClientesComponent), canActivate: [adminGuard] },
      { path: 'cuentas', loadComponent: () => import('./views/cuentas/cuentas.component').then(m => m.CuentasComponent) },
      { path: 'movimientos', loadComponent: () => import('./views/movimientos/movimientos.component').then(m => m.MovimientosComponent) },
      { path: 'reportes', loadComponent: () => import('./views/reportes/reportes.component').then(m => m.ReportesComponent) },
    ]
  },
  { path: '**', redirectTo: 'login' }
];
