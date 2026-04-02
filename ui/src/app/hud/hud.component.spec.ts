import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HudComponent } from './hud.component';
import { HttpClient } from '@angular/common/http';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

describe('HudComponent', () => {
  let component: HudComponent;
  let fixture: ComponentFixture<HudComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HudComponent, FormsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HudComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.perpetrator()).toBe('');
    expect(component.crimeType()).toBe('');
    expect(component.logs()).toEqual([]);
    expect(component.crimesPrevented()).toBe(0);
  });

  it('should update stats periodically', fakeAsync(() => {
    // Initial request from toSignal (via interval(1000) - wait, interval emits after period)
    // Actually, interval(1000) will emit first value after 1000ms.
    
    tick(1100);
    const req = httpMock.expectOne('/api/pre-crime/stats');
    expect(req.request.method).toBe('GET');
    req.flush(42);

    expect(component.crimesPrevented()).toBe(42);
    
    tick(1000);
    const req2 = httpMock.expectOne('/api/pre-crime/stats');
    req2.flush(43);
    expect(component.crimesPrevented()).toBe(43);
    
    httpMock.verify();
  }));

  it('should trigger vision and handle success', () => {
    component.perpetrator.set('John Doe');
    component.crimeType.set('Murder');
    
    component.triggerVision();
    
    expect(component.logs().length).toBe(1);
    expect(component.logs()[0].message).toContain('Vision detected: John Doe');

    const req = httpMock.expectOne(req => req.url.includes('/api/pre-crime/vision'));
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('perpetrator')).toBe('John Doe');
    expect(req.request.params.get('crimeType')).toBe('Murder');
    
    req.flush('Success', { status: 200, statusText: 'OK' });

    expect(component.logs().length).toBe(2);
    expect(component.logs()[0].message).toContain('Arrest warrant issued');
    expect(component.perpetrator()).toBe('');
    expect(component.crimeType()).toBe('');
  });

  it('should handle error when trigger vision fails', () => {
    component.perpetrator.set('John Doe');
    component.crimeType.set('Murder');
    
    component.triggerVision();
    
    const req = httpMock.expectOne(req => req.url.includes('/api/pre-crime/vision'));
    req.error(new ProgressEvent('error'));

    expect(component.logs().length).toBe(2);
    expect(component.logs()[0].message).toContain('ERROR: Temporal paradox detected');
  });
});
