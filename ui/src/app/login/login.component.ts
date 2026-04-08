import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    username = signal('');
    password = signal('');
    error = signal('');

    constructor(private authService: AuthService, private router: Router) {
    }

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
