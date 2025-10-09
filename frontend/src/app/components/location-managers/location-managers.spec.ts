import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationManagers } from './location-managers';

describe('LocationManagers', () => {
  let component: LocationManagers;
  let fixture: ComponentFixture<LocationManagers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LocationManagers]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationManagers);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
