import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Subscription, switchMap } from 'rxjs';

@Component({
  selector: 'app-location-managers',
  templateUrl: './location-managers.html',
  styleUrls: ['./location-managers.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class LocationManagersComponent implements OnInit, OnDestroy {
  locationName: string | null = null;
  managers: any[] = [];
  availableUsers: any[] = [];
  addManagerForm: FormGroup;
  isAdmin: boolean = false;
  private adminSubscription!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.addManagerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.adminSubscription = this.authService.isAdmin$.subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });

    this.locationName = this.route.snapshot.paramMap.get('name');
    if (this.locationName) {
      this.refreshData();
    }
  }

  ngOnDestroy(): void {
    if (this.adminSubscription) {
      this.adminSubscription.unsubscribe();
    }
  }


  onSubmit(): void {
    if (this.addManagerForm.invalid || !this.locationName) {
      return;
    }

    const formData = this.addManagerForm.value;
    this.locationService.addManager(
      this.locationName,
      formData.email
    ).subscribe({
      next: () => {
        console.log('Manager added successfully');
        this.addManagerForm.reset();
        this.refreshData(); // Refresh both managers and available users
      },
      error: (err) => {
        console.error('Error adding manager:', err);
        alert('Failed to add manager. Please try again.');
      }
    });
  }

  removeManager(email: string): void {
    if (!this.locationName) return;
    
    if (confirm(`Are you sure you want to remove ${email} as manager?`)) {
      this.locationService.removeManager(this.locationName, email).subscribe({
        next: () => {
          console.log('Manager removed successfully');
          // Force refresh both managers and available users
          this.refreshData();
        },
        error: (err) => {
          console.error('Error removing manager:', err);
          alert('Failed to remove manager. Please try again.');
        }
      });
    }
  }

  private refreshData(): void {
    if (!this.locationName) return;
    
    // Load managers
    this.locationService.getLocationManagers(this.locationName).subscribe({
      next: (managers) => {
        console.log('Managers after removal:', managers);
        this.managers = managers;
        
        // Then load available users
        if (this.locationName) {
          this.locationService.getAvailableUsers(this.locationName).subscribe({
            next: (availableUsers) => {
              console.log('Available users after removal:', availableUsers);
              this.availableUsers = availableUsers;
            },
            error: (err) => console.error('Error loading available users:', err)
          });
        }
      },
      error: (err) => console.error('Error loading managers:', err)
    });
  }

}
