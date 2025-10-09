import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-location-form',
  templateUrl: './location-form.html',
  styleUrls: ['./location-form.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class LocationFormComponent implements OnInit, OnDestroy {
  locationForm: FormGroup;
  isEditMode: boolean = false;
  locationName: string | null = null;
  selectedImages: string[] = [];
  selectedFiles: File[] = [];
  isAdmin: boolean = false;
  private adminSubscription!: Subscription;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService,
    private authService: AuthService
  ) {
    this.locationForm = this.fb.group({
      name: ['', Validators.required],
      address: ['', Validators.required],
      type: ['', Validators.required],
      description: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.adminSubscription = this.authService.isAdmin$.subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });

    this.locationName = this.route.snapshot.paramMap.get('name');
    this.isEditMode = !!this.locationName;

    if (this.isEditMode && this.locationName) {
      this.locationForm.controls['name'].disable();
      this.locationService.getLocationDetails(this.locationName).subscribe({
        next: (data) => {
          this.locationForm.patchValue({
            name: data.location.name,
            address: data.location.address,
            type: data.location.type,
            description: data.location.description
          });
        },
        error: (err) => console.error(err)
      });
    }
  }

  ngOnDestroy(): void {
    if (this.adminSubscription) {
      this.adminSubscription.unsubscribe();
    }
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files = Array.from(input.files);
    files.forEach((file) => {
      this.selectedFiles.push(file);
      const reader = new FileReader();
      reader.onload = () => {
        const result = reader.result as string;
        this.selectedImages.push(result);
      };
      reader.readAsDataURL(file);
    });

    input.value = '';
  }

  removeImage(index: number): void {
    this.selectedImages.splice(index, 1);
    this.selectedFiles.splice(index, 1);
  }

  onSubmit(): void {
    if (this.locationForm.invalid) {
      return;
    }

    const formData = this.locationForm.getRawValue();

    if (this.isEditMode && this.locationName) {
      this.locationService.updateLocation(this.locationName, {
        address: formData.address,
        type: formData.type,
        description: formData.description
      }).subscribe({
        next: () => {
          // Navigate to /my-locations if user is not admin (i.e., is a manager)
          // Navigate to /locations if user is admin
          const targetRoute = this.isAdmin ? '/locations' : '/my-locations';
          this.router.navigate([targetRoute]);
        },
        error: (err) => console.error(err)
      });
    } else {
      if (this.selectedFiles.length === 0) {
        alert('At least one image is required.');
        return;
      }
      this.locationService.uploadImages(this.selectedFiles).subscribe({
        next: (paths) => {
          const payload = {
            name: formData.name,
            address: formData.address,
            type: formData.type,
            description: formData.description,
            imagePaths: paths
          };
          this.locationService.createLocation(payload).subscribe({
            next: () => this.router.navigate(['/locations']),
            error: (err) => console.error(err)
          });
        },
        error: (err) => console.error(err)
      });
    }
  }
}
