import { Injector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { CuentaService } from './cuenta.service';
import { NotificationService } from './notification.service';

describe('CuentaService', () => {
  let service: CuentaService;
  let notifyService: NotificationService;
  let httpSpy: any;

  beforeEach(() => {
    httpSpy = { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() };
    notifyService = new NotificationService();
    const injector = Injector.create({
      providers: [
        { provide: HttpClient, useValue: httpSpy },
        { provide: NotificationService, useValue: notifyService },
      ]
    });
    service = runInInjectionContext(injector, () => new CuentaService());
  });

  it('should fetch all accounts', (done) => {
    httpSpy.get.mockReturnValue(of({ success: true, data: [{ accountNumber: '123456', accountType: 'Ahorro' }] }));
    service.getAll().subscribe(data => {
      expect(data.length).toBe(1);
      expect(data[0].accountNumber).toBe('123456');
      done();
    });
  });

  it('should create an account and show notification', (done) => {
    const spy = jest.spyOn(notifyService, 'success');
    httpSpy.post.mockReturnValue(of({ success: true, data: { accountNumber: '654321' } }));
    service.create({ accountNumber: '654321', accountType: 'Corriente', initialBalance: 1000, customerId: 'CLI-001', active: true }).subscribe(data => {
      expect(data.accountNumber).toBe('654321');
      expect(spy).toHaveBeenCalledWith('Cuenta creada exitosamente');
      done();
    });
  });

  it('should delete an account', (done) => {
    const spy = jest.spyOn(notifyService, 'success');
    httpSpy.delete.mockReturnValue(of({ success: true, data: null }));
    service.delete('123456').subscribe(() => {
      expect(spy).toHaveBeenCalledWith('Cuenta eliminada');
      done();
    });
  });
});
