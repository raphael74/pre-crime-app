import {Injectable, signal} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private _isAuthenticated = signal<boolean>(false);
    isAuthenticated = this._isAuthenticated.asReadonly();
    private _authHeader = signal<string | null>(null);
    authHeader = this._authHeader.asReadonly();

    login(username: string, password: string): boolean {
        // In a real app, you might want to call an endpoint to verify credentials.
        // For this demo, we'll just set the header.
        const credentials = btoa(`${username}:${password}`);
        this._authHeader.set(`Basic ${credentials}`);
        this._isAuthenticated.set(true);
        return true;
    }

    logout() {
        this._authHeader.set(null);
        this._isAuthenticated.set(false);
    }
}
