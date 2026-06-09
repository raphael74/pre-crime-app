import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {catchError, EMPTY, interval, switchMap} from 'rxjs';
import {toSignal} from '@angular/core/rxjs-interop';
import {AuthService} from '../auth.service';
import {AuditService, CreateVisionRequestCrimeTypeEnum, PreCrimeService} from '../api';

@Component({
    selector: 'app-hud',
    standalone: true,
    templateUrl: './hud.component.html',
    styleUrl: './hud.component.css',
    imports: [CommonModule, FormsModule],
})
export class HudComponent {
    perpetratorLastName = signal('');
    perpetratorFirstName = signal('');
    crimeType = signal('');
    backendError = signal<string | null>(null);

    crimeTypes = Object.keys(CreateVisionRequestCrimeTypeEnum).sort();

    readonly pollingInterval = 2000;

    auditLogs = toSignal(
        interval(this.pollingInterval).pipe(
            switchMap(() => this.auditService.getLogs().pipe(
                catchError(() => {
                    this.backendError.set('AUDIT LOG FETCH FAILED');
                    return EMPTY;
                })
            ))
        ),
        {initialValue: []}
    );

    crimesPrevented = toSignal(
        interval(this.pollingInterval).pipe(
            switchMap(() => this.preCrimeService.getStats().pipe(
                catchError(() => {
                    this.backendError.set('STATUS UPDATE FAILED');
                    return EMPTY;
                })
            ))
        ),
        {initialValue: 0}
    );

    pendingPreArrests = toSignal(
        interval(this.pollingInterval).pipe(
            switchMap(() => this.preCrimeService.getArrestsPending().pipe(
                catchError(() => {
                    this.backendError.set('PRE-ARREST LOG FETCH FAILED');
                    return EMPTY;
                })
            ))
        ),
        {initialValue: []}
    );

    executedPreArrests = toSignal(
        interval(this.pollingInterval).pipe(
            switchMap(() => this.preCrimeService.getArrestsExecuted().pipe(
                catchError(() => {
                    this.backendError.set('PRE-ARREST LOG FETCH FAILED');
                    return EMPTY;
                })
            ))
        ),
        {initialValue: []}
    );

    apologies = toSignal(
        interval(this.pollingInterval).pipe(
            switchMap(() => this.preCrimeService.getApologies().pipe(
                catchError(() => {
                    this.backendError.set('APOLOGY LOG FETCH FAILED');
                    return EMPTY;
                })
            ))
        ),
        {initialValue: []}
    );

    constructor(
        private auditService: AuditService,
        private preCrimeService: PreCrimeService,
        private authService: AuthService,
        private router: Router
    ) {
    }

    formatPayload(payload: string | undefined): string {
        if (!payload) return '';
        try {
            return JSON.stringify(JSON.parse(payload), null, 2);
        } catch (e) {
            return payload;
        }
    }

    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    triggerVision() {
        const perp = this.perpetratorLastName();
        const firstName = this.perpetratorFirstName();
        const crimeType: CreateVisionRequestCrimeTypeEnum = this.getCrimeTypeFromString(this.crimeType());
        if (!perp || !firstName || !crimeType) return;

        this.backendError.set(null);

        this.preCrimeService.createVision({
            perpetratorLastName: perp,
            perpetratorFirstName: firstName,
            crimeType: crimeType
        }).subscribe({
            next: () => {
                this.perpetratorLastName.set('');
                this.perpetratorFirstName.set('');
                this.crimeType.set('');
            },
            error: () => {
                this.backendError.set('VISION TRANSMISSION FAILED');
            }
        });
    }

    executePreArrest(preArrestId: string) {
        this.preCrimeService.arrestExecuted({preArrestId: preArrestId}).subscribe({})
    }

    cancelPreArrest(preArrestId: string) {
        this.preCrimeService.arrestCancelled({preArrestId: preArrestId}).subscribe({})
    }

    private getCrimeTypeFromString(value: string): CreateVisionRequestCrimeTypeEnum {
        return Object.values(CreateVisionRequestCrimeTypeEnum)[Object.keys(CreateVisionRequestCrimeTypeEnum).indexOf(value)]
    }
}
