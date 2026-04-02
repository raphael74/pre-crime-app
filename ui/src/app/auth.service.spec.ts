import {TestBed} from '@angular/core/testing';
import {AuthService} from './auth.service';

describe('AuthService', () => {
    let service: AuthService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(AuthService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should be unauthenticated by default', () => {
        expect(service.isAuthenticated()).toBeFalse();
        expect(service.authHeader()).toBeNull();
    });

    it('should authenticate on login', () => {
        const success = service.login('admin', 'secret');
        expect(success).toBeTrue();
        expect(service.isAuthenticated()).toBeTrue();
        expect(service.authHeader()).toBe('Basic YWRtaW46c2VjcmV0');
    });

    it('should unauthenticate on logout', () => {
        service.login('admin', 'secret');
        service.logout();
        expect(service.isAuthenticated()).toBeFalse();
        expect(service.authHeader()).toBeNull();
    });
});
