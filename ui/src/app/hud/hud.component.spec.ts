import {ComponentFixture, discardPeriodicTasks, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HudComponent} from './hud.component';
import {FormsModule} from '@angular/forms';
import {of, Subject, throwError} from 'rxjs';
import {PreCrimeService} from '../pre-crime.service';

describe('HudComponent', () => {
    let component: HudComponent;
    let fixture: ComponentFixture<HudComponent>;
    let serviceMock: jasmine.SpyObj<PreCrimeService>;
    let statsSubject: Subject<number>;

    beforeEach(async () => {
        statsSubject = new Subject<number>();
        serviceMock = jasmine.createSpyObj('PreCrimeService', ['getStats', 'triggerVision']);
        serviceMock.getStats.and.returnValue(statsSubject.asObservable());

        await TestBed.configureTestingModule({
            imports: [HudComponent, FormsModule],
            providers: [
                {provide: PreCrimeService, useValue: serviceMock}
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
        expect(component.perpetrator()).toBe('');
        expect(component.crimeType()).toBe('');
        expect(component.logs()).toEqual([]);
        expect(component.crimesPrevented()).toBe(0);
        discardPeriodicTasks();
    }));

    it('should update stats periodically', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        tick(1000);
        statsSubject.next(42);
        expect(component.crimesPrevented()).toBe(42);

        tick(1000);
        statsSubject.next(43);
        expect(component.crimesPrevented()).toBe(43);
        discardPeriodicTasks();
    }));

    it('should trigger vision and handle success', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.perpetrator.set('John Doe');
        component.crimeType.set('Murder');

        serviceMock.triggerVision.and.returnValue(of('Success'));

        component.triggerVision();

        // Since of('Success') is synchronous, both logs are added immediately
        expect(component.logs().length).toBe(2);
        expect(component.logs()[1].message).toContain('Vision detected: John Doe');
        expect(component.logs()[0].message).toContain('Arrest warrant issued');

        expect(serviceMock.triggerVision).toHaveBeenCalledWith('John Doe', 'Murder');

        expect(component.perpetrator()).toBe('');
        expect(component.crimeType()).toBe('');
        discardPeriodicTasks();
    }));

    it('should handle error when trigger vision fails', fakeAsync(() => {
        fixture = TestBed.createComponent(HudComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.perpetrator.set('John Doe');
        component.crimeType.set('Murder');

        serviceMock.triggerVision.and.returnValue(throwError(() => new Error('Error')));

        component.triggerVision();

        expect(component.logs().length).toBe(2);
        expect(component.logs()[0].message).toContain('ERROR: Temporal paradox detected');
        discardPeriodicTasks();
    }));
});
