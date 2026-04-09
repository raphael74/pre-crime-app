import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface AuditEntry {
    id: { value: string };
    eventType: string;
    payload: string;
    recordedAt: string;
}

@Injectable({
    providedIn: 'root'
})
export class PreCrimeService {
    constructor(private http: HttpClient) {
    }

    getStats(): Observable<number> {
        return this.http.get<number>('/api/pre-crime/stats');
    }

    getAuditLogs(): Observable<AuditEntry[]> {
        return this.http.get<AuditEntry[]>('/api/audit/logs');
    }

    triggerVision(perp: string, type: string): Observable<string> {
        const params = new HttpParams()
            .set('perpetrator', perp)
            .set('crimeType', type);
        return this.http.post('/api/pre-crime/vision', {}, {params, responseType: 'text'});
    }
}
