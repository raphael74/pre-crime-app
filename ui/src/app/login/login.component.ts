import {Component, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../auth.service';

@Component({
    selector: 'app-login',
    imports: [FormsModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    private authService = inject(AuthService);
    private router = inject(Router);

    username = signal('');
    password = signal('');
    error = signal('');

    onLogin() {
        this.authService.login(this.username(), this.password()).subscribe(success => {
            if (success) {
                this.router.navigate(['/']);
            } else {
                this.error.set('Invalid credentials');
            }
        });
    }
}
