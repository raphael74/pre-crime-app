import {ComponentFixture, TestBed} from '@angular/core/testing';
import {LoginComponent} from './login.component';
import {AuthService} from '../auth.service';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {of} from 'rxjs';
import type {Mock} from 'vitest';

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let authServiceSpy: {login: Mock};
    let routerSpy: {navigate: Mock};

    beforeEach(async () => {
        authServiceSpy = {login: vi.fn()};
        routerSpy = {navigate: vi.fn()};

        await TestBed.configureTestingModule({
            imports: [LoginComponent, FormsModule],
            providers: [
                {provide: AuthService, useValue: authServiceSpy},
                {provide: Router, useValue: routerSpy}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should call authService.login and navigate on success', () => {
        authServiceSpy.login.mockReturnValue(of(true));
        component.username.set('admin');
        component.password.set('password');

        component.onLogin();

        expect(authServiceSpy.login).toHaveBeenCalledWith('admin', 'password');
        expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should set error message on login failure', () => {
        authServiceSpy.login.mockReturnValue(of(false));
        component.username.set('wrong');
        component.password.set('wrong');

        component.onLogin();

        expect(authServiceSpy.login).toHaveBeenCalledWith('wrong', 'wrong');
        expect(component.error()).toBe('Invalid credentials');
        expect(routerSpy.navigate).not.toHaveBeenCalled();
    });
});
