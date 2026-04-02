import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {authGuard} from './auth.guard';
import {AuthService} from './auth.service';

describe('authGuard', () => {
    let authService: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;

    beforeEach(() => {
        authService = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
        router = jasmine.createSpyObj('Router', ['parseUrl']);

        TestBed.configureTestingModule({
            providers: [
                {provide: AuthService, useValue: authService},
                {provide: Router, useValue: router}
            ]
        });
    });

    it('should allow navigation if user is authenticated', () => {
        authService.isAuthenticated.and.returnValue(true);

        const result = TestBed.runInInjectionContext(() => authGuard());

        expect(result).toBeTrue();
    });

    it('should redirect to login if user is not authenticated', () => {
        authService.isAuthenticated.and.returnValue(false);
        const loginUrl = {} as any;
        router.parseUrl.and.returnValue(loginUrl);

        const result = TestBed.runInInjectionContext(() => authGuard());

        expect(router.parseUrl).toHaveBeenCalledWith('/login');
        expect(result).toBe(loginUrl);
    });
});
