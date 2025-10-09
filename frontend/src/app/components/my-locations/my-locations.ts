import { Component, OnInit, OnDestroy } from '@angular/core';
import { LocationService } from '../../services/location.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-my-locations',
  templateUrl: './my-locations.html',
  styleUrls: ['./my-locations.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class MyLocationsComponent implements OnInit, OnDestroy {
  managedLocations: any[] = [];
  isLoggedIn: boolean = false;
  private authSubscription!: Subscription;

  constructor(
    private locationService: LocationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authSubscription = this.authService.loggedIn$.subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        this.loadManagedLocations();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  loadManagedLocations(): void {
    this.locationService.getMyManagedLocations().subscribe({
      next: (data) => {
        console.log('Managed locations loaded:', data);
        this.managedLocations = data;
      },
      error: (err) => console.error('Error loading managed locations:', err)
    });
  }

  getImageUrl(imagePath: string): string {
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    return `http://localhost:8080${imagePath}`;
  }
}
