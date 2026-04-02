import {Component, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {interval, switchMap} from 'rxjs';
import {toSignal} from '@angular/core/rxjs-interop';

@Component({
    selector: 'app-hud',
    standalone: true,
    templateUrl: './hud.component.html',
    imports: [CommonModule, FormsModule],
})
export class HudComponent {
    perpetrator = signal('');
    crimeType = signal('');
    logs = signal<any[]>([]);

    crimesPrevented = toSignal(
        interval(1000).pipe(
            switchMap(() => this.http.get<number>('/api/pre-crime/stats'))
        ),
        {initialValue: 0}
    );

    constructor(private http: HttpClient) {
    }

    triggerVision() {
        const perp = this.perpetrator();
        const type = this.crimeType();
        if (!perp || !type) return;

        this.logs.update(logs => [{
            time: new Date().toLocaleTimeString(),
            message: `Vision detected: ${perp} will commit ${type}`
        }, ...logs]);

        const url = `/api/pre-crime/vision?perpetrator=${perp}&crimeType=${type}`;
        this.http.post(url, {}, {responseType: 'text'}).subscribe({
            next: (res) => {
                this.logs.update(logs => [{
                    time: new Date().toLocaleTimeString(),
                    message: `Arrest warrant issued. Jetpacks deployed.`
                }, ...logs]);
                this.perpetrator.set('');
                this.crimeType.set('');
            },
            error: (err) => {
                this.logs.update(logs => [{
                    time: new Date().toLocaleTimeString(),
                    message: `ERROR: Temporal paradox detected. Check Precog levels.`
                }, ...logs]);
            }
        });
    }
}
