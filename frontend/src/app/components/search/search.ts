import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { SearchService, SearchRequest, SearchResult } from '../../services/search.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './search.html',
  styleUrl: './search.css'
})
export class SearchComponent {

  req: SearchRequest = { operator: 'AND', sortOrder: 'asc' };
  results: SearchResult[] = [];
  searched: boolean = false;
  loading: boolean = false;

  constructor(private searchService: SearchService, private sanitizer: DomSanitizer) {}

  onSubmit(): void {
    this.loading = true;
    this.searchService.search(this.cleanReq()).subscribe({
      next: (data) => {
        this.results = data;
        this.searched = true;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  similar(name: string): void {
    this.loading = true;
    this.searchService.moreLikeThis(name).subscribe({
      next: (data) => {
        this.results = data;
        this.searched = true;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  reset(): void {
    this.req = { operator: 'AND', sortOrder: 'asc' };
    this.results = [];
    this.searched = false;
  }

  // ES vraca snippet sa <em>...</em>; mark-up renderujemo preko innerHTML
  renderSnippet(snippet: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(snippet);
  }

  pdfUrl(pdfKey: string): string {
    if (pdfKey.startsWith('http')) return pdfKey;
    return `http://localhost:8080${pdfKey}`;
  }

  // izbaci prazne stringove iz request-a da backend ne dobije ""
  private cleanReq(): SearchRequest {
    const out: any = {};
    for (const [k, v] of Object.entries(this.req)) {
      if (v === '' || v === null || v === undefined) continue;
      out[k] = v;
    }
    return out;
  }
}
