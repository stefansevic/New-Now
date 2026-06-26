import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SearchRequest {
  name?: string;
  descriptionUi?: string;
  descriptionPdf?: string;
  reviewCountFrom?: number;
  reviewCountTo?: number;
  avgPerformanceFrom?: number;
  avgPerformanceTo?: number;
  avgSoundLightFrom?: number;
  avgSoundLightTo?: number;
  avgVenueFrom?: number;
  avgVenueTo?: number;
  avgOverallFrom?: number;
  avgOverallTo?: number;
  avgTotalFrom?: number;
  avgTotalTo?: number;
  operator?: 'AND' | 'OR';
  sortOrder?: 'asc' | 'desc';
}

export interface SearchResult {
  name: string;
  descriptionUi: string;
  reviewCount: number;
  avgTotal: number;
  pdfKey: string | null;
  highlights: { [field: string]: string[] } | null;
}

@Injectable({ providedIn: 'root' })
export class SearchService {

  private base = '/api/search/locations';

  constructor(private http: HttpClient) {}

  search(req: SearchRequest): Observable<SearchResult[]> {
    return this.http.post<SearchResult[]>(this.base, req);
  }

  moreLikeThis(name: string): Observable<SearchResult[]> {
    return this.http.get<SearchResult[]>(`${this.base}/${encodeURIComponent(name)}/similar`);
  }
}
