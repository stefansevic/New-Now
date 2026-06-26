import { Component, OnInit, OnDestroy } from '@angular/core';
import { LocationService } from '../../services/location.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-locations',
  templateUrl: './locations.html',
  styleUrls: ['./locations.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class LocationsComponent implements OnInit, OnDestroy {
  locations: any[] = [];
  filteredLocations: any[] = [];
  isAdmin: boolean = false;
  private adminSubscription!: Subscription;

  // Filter properties
  searchName: string = '';
  searchAddress: string = '';
  filterType: string = '';

  constructor(private locationService: LocationService, private authService: AuthService) { }

  ngOnInit(): void {
    this.adminSubscription = this.authService.isAdmin$.subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });
    this.loadLocations();
  }

  ngOnDestroy(): void {
    if (this.adminSubscription) {
      this.adminSubscription.unsubscribe();
    }
  }

  loadLocations(): void {
    this.locationService.getLocations().subscribe({
      next: (data) => {
        this.locations = data;
        this.applyFilters();
      },
      error: (err) => console.error(err)
    });
  }

  applyFilters(): void {
    this.filteredLocations = this.locations.filter(item => {
      const location = item.location;
      
      // Filter by name
      const matchesName = !this.searchName || 
        location.name.toLowerCase().includes(this.searchName.toLowerCase());
      
      // Filter by address
      const matchesAddress = !this.searchAddress || 
        location.address.toLowerCase().includes(this.searchAddress.toLowerCase());
      
      // Filter by type
      const matchesType = !this.filterType || 
        location.type.toLowerCase() === this.filterType.toLowerCase();
      
      return matchesName && matchesAddress && matchesType;
    });
  }

  getUniqueTypes(): string[] {
    const types = this.locations.map(item => item.location.type);
    return [...new Set(types)].sort();
  }

  deleteLocation(name: string): void {
    if (confirm('Are you sure you want to delete this location?')) {
      this.locationService.deleteLocation(name).subscribe({
        next: () => {
          console.log('Location deleted successfully');
          this.loadLocations();
        },
        error: (err) => {
          console.error('Error deleting location:', err);
          alert('Failed to delete location. Please try again.');
        }
      });
    }
  }

  getImageUrl(imagePath: string): string {
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    return `http://localhost:8080${imagePath}`;
  }

  getPdfUrl(pdfKey: string): string {
    if (pdfKey.startsWith('http')) return pdfKey;
    return `http://localhost:8080${pdfKey}`;
  }
}
