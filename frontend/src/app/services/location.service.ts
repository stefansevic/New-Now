import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private baseUrl = '/api/locations';

  constructor(private http: HttpClient) { }

  getLocations(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getLocationDetails(name: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${name}`);
  }

  createLocation(location: any): Observable<any> {
    return this.http.post<any>(this.baseUrl, location);
  }

  updateLocation(name: string, location: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${name}`, location);
  }

  deleteLocation(name: string): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${name}`);
  }

  uploadImages(files: File[]): Observable<string[]> {
    const formData = new FormData();
    files.forEach(f => formData.append('files', f));
    return this.http.post<string[]>(`/api/uploads`, formData);
  }
}
