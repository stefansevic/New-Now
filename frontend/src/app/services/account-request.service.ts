import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccountRequestService {
  private baseUrl = 'http://localhost:8081/api/account-requests';

  constructor(private http: HttpClient) { }

  getRequests(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  approveRequest(email: string, name: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${email}/approve`, null, { params: { name } });
  }

  rejectRequest(email: string, reason: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${email}/reject`, null, { params: { reason } });
  }
}
