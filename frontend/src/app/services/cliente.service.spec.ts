import { Injector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { ClienteService } from './cliente.service';
import { NotificationService } from './notification.service';

describe('ClienteService', () => {
  let service: ClienteService;
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
    service = runInInjectionContext(injector, () => new ClienteService());
  });

  it('should fetch all clients', (done) => {
    httpSpy.get.mockReturnValue(of({ success: true, data: [{ customerId: 'CLI-001', name: 'Jose Lema' }] }));
    service.getAll().subscribe(data => {
      expect(data.length).toBe(1);
      expect(data[0].name).toBe('Jose Lema');
      done();
    });
  });

  it('should create a client and show success notification', (done) => {
    const spy = jest.spyOn(notifyService, 'success');
    httpSpy.post.mockReturnValue(of({ success: true, data: { customerId: 'CLI-NEW', name: 'Test' } }));
    service.create({ name: 'Test', gender: 'Male', age: 25, identification: '456', address: 'GYE', phone: '098' }).subscribe(data => {
      expect(data.customerId).toBe('CLI-NEW');
      expect(spy).toHaveBeenCalledWith('Cliente creado exitosamente');
      done();
    });
  });

  it('should show error notification on failure', (done) => {
    const spy = jest.spyOn(notifyService, 'error');
    httpSpy.get.mockReturnValue(throwError(() => ({ error: { message: 'Server error' } })));
    service.getAll().subscribe({
      error: () => { expect(spy).toHaveBeenCalledWith('Server error'); done(); }
    });
  });

  it('should delete a client', (done) => {
    const spy = jest.spyOn(notifyService, 'success');
    httpSpy.delete.mockReturnValue(of({ success: true, data: null }));
    service.delete('CLI-001').subscribe(() => {
      expect(spy).toHaveBeenCalledWith('Cliente eliminado');
      done();
    });
  });
});
