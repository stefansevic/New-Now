import { Component, OnInit, OnDestroy } from '@angular/core';
import { LocationService } from '../../services/location.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-locations',
  templateUrl: './locations.html',
  styleUrls: ['./locations.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class LocationsComponent implements OnInit, OnDestroy {
  locations: any[] = [];
  isAdmin: boolean = false;
  private adminSubscription!: Subscription;

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
      next: (data) => this.locations = data,
      error: (err) => console.error(err)
    });
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
}
