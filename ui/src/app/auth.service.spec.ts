import {TestBed} from '@angular/core/testing';
import {AuthService} from './auth.service';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {UserService} from './api';

describe('AuthService', () => {
    let service: AuthService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                AuthService,
                UserService,
                provideHttpClient(),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(AuthService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should be unauthenticated by default', () => {
        expect(service.isAuthenticated()).toBeFalse();
        expect(service.authHeader()).toBeNull();
    });

    it('should authenticate on login', () => {
        service.login('admin', 'secret').subscribe(success => {
            expect(success).toBeTrue();
            expect(service.isAuthenticated()).toBeTrue();
            expect(service.authHeader()).toBe('Basic YWRtaW46c2VjcmV0');
        });

        const req = httpMock.expectOne('/api/user');
        expect(req.request.method).toBe('GET');
        expect(req.request.headers.get('Authorization')).toBe('Basic YWRtaW46c2VjcmV0');
        req.flush({username: 'admin'});
    });

    it('should fail on login error', () => {
        service.login('admin', 'wrong').subscribe(success => {
            expect(success).toBeFalse();
            expect(service.isAuthenticated()).toBeFalse();
            expect(service.authHeader()).toBeNull();
        });

        const req = httpMock.expectOne('/api/user');
        req.error(new ErrorEvent('Unauthorized'), {status: 401});
    });

    it('should unauthenticate on logout', () => {
        service.login('admin', 'secret').subscribe();
        let req = httpMock.expectOne('/api/user');
        req.flush({username: 'admin'});

        service.logout();
        req = httpMock.expectOne('/api/logout');
        expect(req.request.method).toBe('POST');
        req.flush({});

        expect(service.isAuthenticated()).toBeFalse();
        expect(service.authHeader()).toBeNull();
    });
});
