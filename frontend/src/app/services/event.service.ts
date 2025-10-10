import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EventService {

  private baseUrl = '/api/events';

  constructor(private http: HttpClient) { }

  createEvent(event: any): Observable<any> {
    return this.http.post<any>(this.baseUrl, event);
  }

  updateEvent(name: string, event: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${name}`, event);
  }

  deleteEvent(name: string): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${name}`);
  }

  getEventsByLocation(locationName: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/location/${locationName}`);
  }

  getEvent(name: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${name}`);
  }
}

