import {Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, map, Observable, of, tap} from 'rxjs';
import {UserService} from './api';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private _isAuthenticated = signal<boolean>(false);
    isAuthenticated = this._isAuthenticated.asReadonly();
    private _authHeader = signal<string | null>(null);
    authHeader = this._authHeader.asReadonly();

    constructor(private http: HttpClient, private userService: UserService) {
    }

    login(username: string, password: string): Observable<boolean> {
        const credentials = btoa(`${username}:${password}`);
        const authHeader = `Basic ${credentials}`;

        // Temporarily set header to allow the call through the interceptor
        // Or better, we explicitly set it here for this call
        return this.http.get<any>('/api/user', {
            headers: {'Authorization': authHeader}
        }).pipe(
            tap(() => {
                this._authHeader.set(authHeader);
                this._isAuthenticated.set(true);
            }),
            map(() => true),
            catchError(() => {
                this.clearSession();
                return of(false);
            })
        );
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
