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
          list: () => of([])
        }
      }]
    }).compileComponents();
  });

  it('should create the app and render the signed-in account', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Welcome, Care User.');
  });
});
