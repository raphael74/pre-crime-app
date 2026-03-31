import {Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
    selector: 'app-root',
    standalone: true,
    templateUrl: './app.component.html',
    imports: [CommonModule, FormsModule],
})
export class AppComponent {
    perpetrator = '';
    crimeType = '';
    crimesPrevented = 42;
    logs: any[] = [
        {time: '08:45', message: 'Scanning temporal rift...'},
        {time: '08:46', message: 'Agatha is dreaming...'}
    ];

    constructor(private http: HttpClient) {
    }

    triggerVision() {
        if (!this.perpetrator || !this.crimeType) return;

        this.logs.unshift({
            time: new Date().toLocaleTimeString(),
            message: `Vision detected: ${this.perpetrator} will commit ${this.crimeType}`
        });

        const url = `/api/pre-crime/vision?perpetrator=${this.perpetrator}&crimeType=${this.crimeType}`;

        this.http.post(url, {}, {responseType: 'text'}).subscribe({
            next: (res) => {
                this.logs.unshift({
                    time: new Date().toLocaleTimeString(),
                    message: `Arrest warrant issued. Jetpacks deployed.`
                });
                this.crimesPrevented++;
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
