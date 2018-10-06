import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TweetsFormComponent } from './tweets-form.component';

describe('TweetsFormComponent', () => {
  let component: TweetsFormComponent;
  let fixture: ComponentFixture<TweetsFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TweetsFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TweetsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
