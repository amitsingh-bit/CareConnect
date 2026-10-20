import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './auth.service';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [{
        provide: AuthService,
        useValue: {
          me: () => of({ id: 1, profileId: 1, name: 'Care User', email: 'care@example.com', role: 'PATIENT' }),
          list: () => of([]),
          get: () => of({ id: 1, name: 'Care User', age: 42, gender: 'Not recorded' })
        }
      }]
    }).compileComponents();
  });

  it('should create the app and render the patient overview', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('YOUR CARE, IN ONE PLACE');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No upcoming appointments');
  });

  it('switches app navigation to Hindi and keeps the selected language', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    fixture.componentInstance.setAssistantLanguage('hi');
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('अवलोकन');
    expect((fixture.nativeElement as HTMLElement).querySelector('main')?.getAttribute('lang')).toBe('hi');
    expect(localStorage.getItem('careconnect.language')).toBe('hi');
  });
});
