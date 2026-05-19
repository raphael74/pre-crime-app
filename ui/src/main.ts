import {bootstrapApplication} from '@angular/platform-browser';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {provideRouter} from '@angular/router';
import {AppComponent} from './app/app.component';
import {routes} from './app/app.routes';
import {authInterceptor} from './app/auth.interceptor';
import {provideApi} from './app/api/provide-api';

bootstrapApplication(AppComponent, {
    providers: [
        provideHttpClient(
            withInterceptors([authInterceptor])
        ),
        provideRouter(routes),
        provideApi('/api')
    ]
}).catch(err => console.error(err));
