import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {catchError, EMPTY, interval, switchMap} from 'rxjs';
import {toSignal} from '@angular/core/rxjs-interop';
import {PreCrimeService} from '../pre-crime.service';
import {AuthService} from '../auth.service';

@Component({
    selector: 'app-hud',
    standalone: true,
    templateUrl: './hud.component.html',
    styleUrl: './hud.component.css',
    imports: [CommonModule, FormsModule],
})
export class HudComponent {
    perpetrator = signal('');
    crimeType = signal('');
    logs = signal<any[]>([]);
    backendError = signal<string | null>(null);

    crimesPrevented = toSignal(
        interval(1000).pipe(
            switchMap(() => this.service.getStats().pipe(
                catchError(() => {
                    this.backendError.set('STATUS UPDATE FAILED');
                    return EMPTY;
                })
            ))
        ),
        {initialValue: 0}
    );

    constructor(
        private service: PreCrimeService,
        private authService: AuthService,
        private router: Router
    ) {
    }

    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    triggerVision() {
        const perp = this.perpetrator();
        const type = this.crimeType();
        if (!perp || !type) return;

        this.backendError.set(null);
        this.logs.update(logs => [{
            time: new Date().toLocaleTimeString(),
            message: `Vision detected: ${perp} will commit ${type}`
        }, ...logs]);

        this.service.triggerVision(perp, type).subscribe({
            next: () => {
                this.logs.update(logs => [{
                    time: new Date().toLocaleTimeString(),
                    message: `Arrest warrant issued. Jetpacks deployed.`
                }, ...logs]);
                this.perpetrator.set('');
                this.crimeType.set('');
            },
            error: () => {
                this.backendError.set('VISION TRANSMISSION FAILED');
                this.logs.update(logs => [{
                    time: new Date().toLocaleTimeString(),
                    message: `ERROR: Vision Transmission failed.`
                }, ...logs]);
            }
        });
    }
}
