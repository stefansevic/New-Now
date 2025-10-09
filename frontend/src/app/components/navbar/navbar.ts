import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LocationService } from '../../services/location.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent implements OnInit, OnDestroy {
  isLoggedIn: boolean = false;
  isAdmin: boolean = false;
  isManager: boolean = false;
  private authSubscription!: Subscription;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.authSubscription = this.authService.loggedIn$.subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        this.checkIfManager();
      } else {
        this.isManager = false;
      }
    });
    this.authSubscription.add(this.authService.isAdmin$.subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    }));
  }

  private checkIfManager(): void {
    this.locationService.getMyManagedLocations().subscribe({
      next: (locations) => {
        this.isManager = locations.length > 0;
      },
      error: (err) => {
        console.error('Error checking manager status:', err);
        this.isManager = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  get homeRoute(): string {
    const isBackendAdmin = this.authService.getUserName() === 'admin@system.local';
    return isBackendAdmin ? '/admin/home' : '/home';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
