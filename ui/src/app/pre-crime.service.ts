import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface AuditEntry {
    id: { value: string };
    eventType: string;
    payload: string;
    recordedAt: string;
}

export interface CreateVisionResponse {
    visionId: string;
    message: string;
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

    triggerVision(perpetrator: string, crimeType: string): Observable<CreateVisionResponse> {
        return this.http.post<CreateVisionResponse>('/api/pre-crime/vision', {perpetrator, crimeType});
    }
}
