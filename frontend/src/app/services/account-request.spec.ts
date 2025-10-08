import { TestBed } from '@angular/core/testing';

import { AccountRequest } from './account-request';

describe('AccountRequest', () => {
  let service: AccountRequest;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AccountRequest);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
