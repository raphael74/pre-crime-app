import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {interval, Subscription} from 'rxjs';

@Component({
    selector: 'app-hud',
    standalone: true,
    templateUrl: './hud.component.html',
    imports: [CommonModule, FormsModule],
})
export class HudComponent implements OnInit, OnDestroy {
    perpetrator = '';
    crimeType = '';
    crimesPrevented = 0;
    logs: any[] = [];
    private statsSubscription?: Subscription;

    constructor(private http: HttpClient) {
    }

    ngOnInit() {
        this.fetchStats();
        // Poll for stats updates every 5 seconds to reflect asynchronous processing
        this.statsSubscription = interval(1000).subscribe(() => this.fetchStats());
    }

    ngOnDestroy() {
        this.statsSubscription?.unsubscribe();
    }

    fetchStats() {
        this.http.get<number>('/api/pre-crime/stats').subscribe({
            next: (count) => this.crimesPrevented = count,
            error: (err) => console.error('Failed to fetch stats', err)
        });
    }

    triggerVision() {
        if (!this.perpetrator || !this.crimeType) return;

        this.logs.unshift({
            time: new Date().toLocaleTimeString(),
            message: `Vision detected: ${this.perpetrator} will commit ${this.crimeType}`
        });

        const url = `/api/pre-crime/vision?perpetrator=${this.perpetrator}&crimeType=${this.crimeType}`;
        console.log('post sent!')
        this.http.post(url, {}, {responseType: 'text'}).subscribe({
            next: (res) => {
                console.log('post returned!')
                this.logs.unshift({
                    time: new Date().toLocaleTimeString(),
                    message: `Arrest warrant issued. Jetpacks deployed.`
                });
                this.perpetrator = '';
                this.crimeType = '';
            },
            error: (err) => {
                this.logs.unshift({
                    time: new Date().toLocaleTimeString(),
                    message: `ERROR: Temporal paradox detected. Check Precog levels.`
                });
            }
        });
    }
}
