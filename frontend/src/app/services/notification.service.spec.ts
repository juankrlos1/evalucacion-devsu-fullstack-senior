import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => { service = new NotificationService(); });

  it('should add and auto-dismiss notifications', (done) => {
    service.show('Test', 'info', 100);
    expect(service.notifications().length).toBe(1);
    expect(service.notifications()[0].message).toBe('Test');

    setTimeout(() => {
      expect(service.notifications().length).toBe(0);
      done();
    }, 200);
  });

  it('should dismiss manually', () => {
    service.show('Manual', 'success', 99999);
    const id = service.notifications()[0].id;
    service.dismiss(id);
    expect(service.notifications().length).toBe(0);
  });

  it('should show success/error/warning shortcuts', () => {
    service.success('ok');
    expect(service.notifications()[0].type).toBe('success');
    service.error('fail');
    expect(service.notifications()[1].type).toBe('error');
    service.warning('warn');
    expect(service.notifications()[2].type).toBe('warning');
  });
});

