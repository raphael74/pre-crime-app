import {Injectable, signal} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {interval, Observable, startWith, switchMap} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PreCrimeService {
    constructor(private http: HttpClient) {}

    getStats(): Observable<number> {
        return this.http.get<number>('/api/pre-crime/stats');
    }

    triggerVision(perp: string, type: string): Observable<string> {
        const params = new HttpParams()
            .set('perpetrator', perp)
            .set('crimeType', type);
        return this.http.post('/api/pre-crime/vision', {}, {params, responseType: 'text'});
    }
}
