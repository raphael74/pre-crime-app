import {Routes} from '@angular/router';
import {authGuard} from './auth.guard';

export const routes: Routes = [
    {path: '', loadComponent: () => import('./hud/hud.component').then(m => m.HudComponent), canActivate: [authGuard]},
    {path: 'login', loadComponent: () => import('./login/login.component').then(m => m.LoginComponent)},
    {path: '**', redirectTo: ''}
];
