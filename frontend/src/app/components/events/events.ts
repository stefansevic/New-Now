import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-events',
  templateUrl: './events.html',
  styleUrls: ['./events.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class EventsComponent implements OnInit {
  events: any[] = [];
  filteredEvents: any[] = [];
  loading: boolean = true;

  // Filter properties
  filterType: string = '';
  filterLocation: string = '';
  searchAddress: string = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  showOnlyToday: boolean = true;

  constructor(private eventService: EventService) { }

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getAllUpcomingEvents().subscribe({
      next: (data) => {
        console.log('Upcoming events loaded:', data);
        this.events = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading events:', err);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    this.filteredEvents = this.events.filter(event => {
      // Filter by today's date if enabled
      if (this.showOnlyToday) {
        const eventDate = new Date(event.date);
        eventDate.setHours(0, 0, 0, 0);
        if (eventDate.getTime() !== today.getTime()) {
          return false;
        }
      }

      // Filter by type
      const matchesType = !this.filterType || 
        event.type.toLowerCase() === this.filterType.toLowerCase();

      // Filter by location
      const matchesLocation = !this.filterLocation || 
        event.location?.name.toLowerCase().includes(this.filterLocation.toLowerCase());

      // Filter by address
      const matchesAddress = !this.searchAddress || 
        event.address.toLowerCase().includes(this.searchAddress.toLowerCase());

      // Filter by price range
      const eventPrice = event.price || 0;
      const matchesMinPrice = this.minPrice === null || eventPrice >= this.minPrice;
      const matchesMaxPrice = this.maxPrice === null || eventPrice <= this.maxPrice;

      return matchesType && matchesLocation && matchesAddress && matchesMinPrice && matchesMaxPrice;
    });
  }

  getUniqueTypes(): string[] {
    const types = this.events.map(event => event.type);
    return [...new Set(types)].sort();
  }

  getUniqueLocations(): string[] {
    const locations = this.events
      .map(event => event.location?.name)
      .filter(name => name);
    return [...new Set(locations)].sort();
  }

  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    return `http://localhost:8080${imagePath}`;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }
}

