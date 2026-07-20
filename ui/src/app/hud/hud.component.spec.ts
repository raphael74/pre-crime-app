import {ComponentFixture, discardPeriodicTasks, fakeAsync, TestBed, tick} from '@angular/core/testing';
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

describe('HudComponent', () => {
    let component: HudComponent;
    let fixture: ComponentFixture<HudComponent>;
    let auditServiceMock: jasmine.SpyObj<AuditService>;
    let preCrimeServiceMock: jasmine.SpyObj<PreCrimeService>;
    let authServiceMock: jasmine.SpyObj<AuthService>;
    let routerMock: jasmine.SpyObj<Router>;
    let statsSubject: Subject<number>;
    let auditLogsSubject: Subject<AuditEntry[]>;

    beforeEach(async () => {
        statsSubject = new Subject<number>();
        auditLogsSubject = new Subject<AuditEntry[]>();

        auditServiceMock = jasmine.createSpyObj('AuditService', ['getLogs']);
        auditServiceMock.getLogs.and.returnValue(auditLogsSubject.asObservable() as any);

        preCrimeServiceMock = jasmine.createSpyObj('PreCrimeService', ['getStats', 'createVision', 'getApologies', 'getArrestsExecuted', 'getArrestsPending']);
        preCrimeServiceMock.getStats.and.returnValue(statsSubject.asObservable() as any);
        preCrimeServiceMock.getApologies.and.returnValue(of([]) as any);
        preCrimeServiceMock.getArrestsExecuted.and.returnValue(of([]) as any);
        preCrimeServiceMock.getArrestsPending.and.returnValue(of([]) as any);

        authServiceMock = jasmine.createSpyObj('AuthService', ['logout']);
        routerMock = jasmine.createSpyObj('Router', ['navigate']);

        await TestBed.configureTestingModule({
            imports: [HudComponent, FormsModule],
            providers: [
                {provide: AuditService, useValue: auditServiceMock},
                {provide: PreCrimeService, useValue: preCrimeServiceMock},
                {provide: AuthService, useValue: authServiceMock},
                {provide: Router, useValue: routerMock}
            ]
        }).compileComponents();
    });

    it('should create', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        expect(component).toBeTruthy();
        discardPeriodicTasks();
    }));

    it('should initialize with default values', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        expect(component.perpetratorFirstName()).toBe('');
        expect(component.perpetratorLastName()).toBe('');
        expect(component.crimeType()).toBe('');
        expect(component.auditLogs()).toEqual([]);
        expect(component.crimesPrevented()).toBe(0);
        discardPeriodicTasks();
    }));

    it('should update stats periodically', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        tick(component.pollingInterval);
        statsSubject.next(42);
        expect(component.crimesPrevented()).toBe(42);

        tick(component.pollingInterval);
        statsSubject.next(43);
        expect(component.crimesPrevented()).toBe(43);
        discardPeriodicTasks();
    }));

    it('should update pre-arrests periodically', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        const arrestsSubject = new Subject<any[]>();
        preCrimeServiceMock.getArrestsExecuted.and.returnValue(arrestsSubject.asObservable() as any);
        fixture.detectChanges();

        tick(component.pollingInterval);
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
        discardPeriodicTasks();
    }));

    it('should update audit logs periodically', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        tick(component.pollingInterval);
        const mockLogs: AuditEntry[] = [{
            id: {value: '1'},
            eventType: 'VISION_DETECTED',
            payload: '{"perpetrator": "John Doe"}',
            recordedAt: new Date().toISOString()
        }];
        auditLogsSubject.next(mockLogs);
        expect(component.auditLogs()).toEqual(mockLogs);

        discardPeriodicTasks();
    }));

    it('should trigger vision and handle success', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.perpetratorFirstName.set('John');
        component.perpetratorLastName.set('Doe');
        component.crimeType.set('Murder');

        preCrimeServiceMock.createVision.and.returnValue(of({visionId: 'uuid', message: 'Success'}) as any);

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

        discardPeriodicTasks();
    }));

    it('should handle error when trigger vision fails', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.perpetratorFirstName.set('John');
        component.perpetratorLastName.set('Doe');
        component.crimeType.set('Murder');

        preCrimeServiceMock.createVision.and.returnValue(throwError(() => new Error('Error')));

        component.triggerVision();

        expect(component.backendError()).toBe('VISION TRANSMISSION FAILED');
        discardPeriodicTasks();
    }));

    it('should handle logout', () => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;

        component.logout();

        expect(authServiceMock.logout).toHaveBeenCalled();
        expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
    });
});
