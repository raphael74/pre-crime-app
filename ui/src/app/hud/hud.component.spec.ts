import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HudComponent} from './hud.component';
import {FormsModule} from '@angular/forms';
import {of, Subject, throwError} from 'rxjs';
import {
    AuditEntry,
    AuditService,
    CreateVisionRequestCrimeTypeEnum,
    PreArrestResponseStatusEnum,
    PreCrimeService
} from '../api';
import {AuthService} from '../auth.service';
import {Router} from '@angular/router';
import type {Mock} from 'vitest';

describe('HudComponent', () => {
    let component: HudComponent;
    let fixture: ComponentFixture<HudComponent>;
    let auditServiceMock: {getLogs: Mock};
    let preCrimeServiceMock: {
        getStats: Mock;
        createVision: Mock;
        getApologies: Mock;
        getArrestsExecuted: Mock;
        getArrestsPending: Mock;
    };
    let authServiceMock: {logout: Mock};
    let routerMock: {navigate: Mock};
    let statsSubject: Subject<number>;
    let auditLogsSubject: Subject<AuditEntry[]>;

    beforeEach(async () => {
        statsSubject = new Subject<number>();
        auditLogsSubject = new Subject<AuditEntry[]>();

        auditServiceMock = {getLogs: vi.fn()};
        auditServiceMock.getLogs.mockReturnValue(auditLogsSubject.asObservable() as any);

        preCrimeServiceMock = {
            getStats: vi.fn(),
            createVision: vi.fn(),
            getApologies: vi.fn(),
            getArrestsExecuted: vi.fn(),
            getArrestsPending: vi.fn()
        };
        preCrimeServiceMock.getStats.mockReturnValue(statsSubject.asObservable() as any);
        preCrimeServiceMock.getApologies.mockReturnValue(of([]) as any);
        preCrimeServiceMock.getArrestsExecuted.mockReturnValue(of([]) as any);
        preCrimeServiceMock.getArrestsPending.mockReturnValue(of([]) as any);

        authServiceMock = {logout: vi.fn()};
        routerMock = {navigate: vi.fn()};

        await TestBed.configureTestingModule({
            imports: [HudComponent, FormsModule],
            providers: [
                {provide: AuditService, useValue: auditServiceMock},
                {provide: PreCrimeService, useValue: preCrimeServiceMock},
                {provide: AuthService, useValue: authServiceMock},
                {provide: Router, useValue: routerMock}
            ]
        }).compileComponents();

        // The component polls the backend on an interval; use Vitest's fake timers
        // so tests can advance the clock deterministically (fakeAsync is unsupported
        // by the Vitest test runner).
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should create', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        expect(component.perpetratorFirstName()).toBe('');
        expect(component.perpetratorLastName()).toBe('');
        expect(component.crimeType()).toBe('');
        expect(component.auditLogs()).toEqual([]);
        expect(component.crimesPrevented()).toBe(0);
    });

    it('should update stats periodically', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        vi.advanceTimersByTime(component.pollingInterval);
        statsSubject.next(42);
        expect(component.crimesPrevented()).toBe(42);

        vi.advanceTimersByTime(component.pollingInterval);
        statsSubject.next(43);
        expect(component.crimesPrevented()).toBe(43);
    });

    it('should update pre-arrests periodically', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        const arrestsSubject = new Subject<any[]>();
        preCrimeServiceMock.getArrestsExecuted.mockReturnValue(arrestsSubject.asObservable() as any);
        fixture.detectChanges();

        vi.advanceTimersByTime(component.pollingInterval);
        const mockArrests = [{
            id: '1',
            visionId: 'v1',
            perpetratorId: 'p1',
            firstName: 'John',
            lastName: 'Doe',
            preArrestDate: new Date().toISOString(),
            status: PreArrestResponseStatusEnum.ArrestedBeforeCrime
        }];
        arrestsSubject.next(mockArrests);
        expect(component.executedPreArrests()).toEqual(mockArrests as any);
    });

    it('should update audit logs periodically', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        vi.advanceTimersByTime(component.pollingInterval);
        const mockLogs: AuditEntry[] = [{
            id: {value: '1'},
            eventType: 'VISION_DETECTED',
            payload: '{"perpetrator": "John Doe"}',
            recordedAt: new Date().toISOString()
        }];
        auditLogsSubject.next(mockLogs);
        expect(component.auditLogs()).toEqual(mockLogs);
    });

    it('should trigger vision and handle success', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.perpetratorFirstName.set('John');
        component.perpetratorLastName.set('Doe');
        component.crimeType.set('Murder');

        preCrimeServiceMock.createVision.mockReturnValue(of({visionId: 'uuid', message: 'Success'}) as any);

        component.triggerVision();

        expect(preCrimeServiceMock.createVision).toHaveBeenCalledWith({
            perpetratorFirstName: 'John',
            perpetratorLastName: 'Doe',
            crimeType: CreateVisionRequestCrimeTypeEnum.Murder
        });
        expect(component.perpetratorFirstName()).toBe('');
        expect(component.perpetratorLastName()).toBe('');
        expect(component.crimeType()).toBe('');
        expect(component.backendError()).toBeNull();
    });

    it('should handle error when trigger vision fails', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.perpetratorFirstName.set('John');
        component.perpetratorLastName.set('Doe');
        component.crimeType.set('Murder');

        preCrimeServiceMock.createVision.mockReturnValue(throwError(() => new Error('Error')));

        component.triggerVision();

        expect(component.backendError()).toBe('VISION TRANSMISSION FAILED');
    });

    it('should handle logout', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;

        component.logout();

        expect(authServiceMock.logout).toHaveBeenCalled();
        expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
    });
});
