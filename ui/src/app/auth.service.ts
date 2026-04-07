import {Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private _isAuthenticated = signal<boolean>(false);
    isAuthenticated = this._isAuthenticated.asReadonly();
    private _authHeader = signal<string | null>(null);
    authHeader = this._authHeader.asReadonly();

    constructor(private http: HttpClient) {
    }

    login(username: string, password: string): boolean {
        // In a real app, you might want to call an endpoint to verify credentials.
        // For this demo, we'll just set the header.
        const credentials = btoa(`${username}:${password}`);
        this._authHeader.set(`Basic ${credentials}`);
        this._isAuthenticated.set(true);
        return true;
    }

    logout() {
        // We call the backend to invalidate the session.
        // We don't necessarily wait for it to clear the local state to keep the UI snappy.
        this.http.post('/api/logout', {}).subscribe();
        this.clearSession();
    }

    private clearSession() {
        this._authHeader.set(null);
        this._isAuthenticated.set(false);
    }
}
