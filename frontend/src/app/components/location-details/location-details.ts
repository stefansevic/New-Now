import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.html',
  styleUrls: ['./location-details.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class LocationDetailsComponent implements OnInit, OnDestroy {
  location: any;
  isAdmin: boolean = false;
  private adminSubscription!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.adminSubscription = this.authService.isAdmin$.subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });

    const name = this.route.snapshot.paramMap.get('name');
    if (name) {
      this.locationService.getLocationDetails(name).subscribe({
        next: (data) => this.location = data,
        error: (err) => console.error(err)
      });
    }
  }

  ngOnDestroy(): void {
    if (this.adminSubscription) {
      this.adminSubscription.unsubscribe();
    }
  }

  deleteLocation(): void {
    if (confirm('Are you sure you want to delete this location?')) {
      this.locationService.deleteLocation(this.location.location.name).subscribe({
        next: () => {
          console.log('Location deleted successfully');
          this.router.navigate(['/locations']);
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
