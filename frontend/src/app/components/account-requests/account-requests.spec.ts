import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountRequests } from './account-requests';

describe('AccountRequests', () => {
  let component: AccountRequests;
  let fixture: ComponentFixture<AccountRequests>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountRequests]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountRequests);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
