import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import type {Mock} from 'vitest';
import {authGuard} from './auth.guard';
import {AuthService} from './auth.service';

describe('authGuard', () => {
    let authService: {isAuthenticated: Mock};
    let router: {parseUrl: Mock};

    beforeEach(() => {
        authService = {isAuthenticated: vi.fn()};
        router = {parseUrl: vi.fn()};

        TestBed.configureTestingModule({
            providers: [
                {provide: AuthService, useValue: authService},
                {provide: Router, useValue: router}
            ]
        });
    });

    it('should allow navigation if user is authenticated', () => {
        authService.isAuthenticated.mockReturnValue(true);

        const result = TestBed.runInInjectionContext(() => authGuard());

        expect(result).toBe(true);
    });

    it('should redirect to login if user is not authenticated', () => {
        authService.isAuthenticated.mockReturnValue(false);
        const loginUrl = {} as any;
        router.parseUrl.mockReturnValue(loginUrl);

        const result = TestBed.runInInjectionContext(() => authGuard());

        expect(router.parseUrl).toHaveBeenCalledWith('/login');
        expect(result).toBe(loginUrl);
    });
});
