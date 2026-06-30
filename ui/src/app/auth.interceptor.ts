import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const authHeader = authService.authHeader();

    if (authHeader && req.url.startsWith('/api/')) {
        const authReq = req.clone({
            headers: req.headers.set('Authorization', authHeader)
        });
        return next(authReq);
    }

    return next(req);
};
