import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmissionViewComponent } from './submission-view.component';

describe('SubmissionViewComponent', () => {
  let component: SubmissionViewComponent;
  let fixture: ComponentFixture<SubmissionViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubmissionViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubmissionViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
