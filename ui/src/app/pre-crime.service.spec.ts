import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {PreCrimeService} from './pre-crime.service';

describe('PreCrimeService', () => {
    let service: PreCrimeService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                PreCrimeService,
                provideHttpClient(),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(PreCrimeService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should get stats', () => {
        const dummyStats = 42;

        service.getStats().subscribe(stats => {
            expect(stats).toBe(dummyStats);
        });

        const req = httpMock.expectOne('/api/pre-crime/stats');
        expect(req.request.method).toBe('GET');
        req.flush(dummyStats);
    });

    it('should trigger vision', () => {
        const perp = 'John Doe';
        const type = 'Murder';
        const dummyResponse = 'Success';

        service.triggerVision(perp, type).subscribe(response => {
            expect(response).toBe(dummyResponse);
        });

        const req = httpMock.expectOne(req => req.url === '/api/pre-crime/vision');
        expect(req.request.method).toBe('POST');
        expect(req.request.params.get('perpetrator')).toBe(perp);
        expect(req.request.params.get('crimeType')).toBe(type);
        req.flush(dummyResponse);
    });
});
