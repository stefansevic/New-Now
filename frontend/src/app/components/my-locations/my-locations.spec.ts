import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyLocations } from './my-locations';

describe('MyLocations', () => {
  let component: MyLocations;
  let fixture: ComponentFixture<MyLocations>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyLocations]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyLocations);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
