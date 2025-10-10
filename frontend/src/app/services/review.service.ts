import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private baseUrl = '/api/reviews';

  constructor(private http: HttpClient) { }

  createReview(review: any): Observable<any> {
    return this.http.post<any>(this.baseUrl, review);
  }

  getReviewsByLocation(locationName: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/location/${locationName}`);
  }

  getEligibleEvents(locationName: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/location/${locationName}/eligible-events`);
  }
}

